package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.ChargeMachineDTO;
import com.projet3.atelierdeproduction.DTO.MaintenanceRequestDTO;
import com.projet3.atelierdeproduction.DTO.MaintenanceResponseDTO;
import com.projet3.atelierdeproduction.Entity.MachineEntity;
import com.projet3.atelierdeproduction.Entity.MaintenanceEntity;
import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import com.projet3.atelierdeproduction.Repository.MachineRepository;
import com.projet3.atelierdeproduction.Repository.MaintenanceRepository;
import com.projet3.atelierdeproduction.Repository.OrdreFabricationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ChargeMachineService {

    // F19 - heures ouvrables : du lundi au vendredi, de 8h à 16h
    private static final LocalTime OUVERTURE = LocalTime.of(8, 0);
    private static final LocalTime FERMETURE = LocalTime.of(16, 0);

    private final MachineRepository machineRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final OrdreFabricationRepository ordreFabricationRepository;

    // F19 - POST /machines/{id}/maintenances : enregistre un arrêt de maintenance (400 si la fin précède le début)
    public MaintenanceResponseDTO ajouterMaintenance(Long machineId, MaintenanceRequestDTO dto) {
        MachineEntity machine = trouverOu404(machineRepository, machineId, "Machine");
        if (!dto.getFin().isAfter(dto.getDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin de l'arrêt doit être après son début");
        }
        MaintenanceEntity maintenance = new MaintenanceEntity();
        maintenance.setMachine(machine);
        maintenance.setDebut(dto.getDebut());
        maintenance.setFin(dto.getFin());
        maintenance.setMotif(dto.getMotif());
        return maintenanceVersDTO(maintenanceRepository.save(maintenance));
    }

    // F19 - GET /machines/{id}/maintenances : arrêts de la machine
    public List<MaintenanceResponseDTO> maintenances(Long machineId) {
        trouverOu404(machineRepository, machineId, "Machine");
        return maintenanceRepository.findByMachineIdOrderByDebutAsc(machineId).stream().map(this::maintenanceVersDTO).toList();
    }

    // F19 - GET /machines/{id}/charge?du=&au= : heures occupées par les ordres de la période rapportées aux heures ouvrables,
    // les arrêts de maintenance étant retirés du dénominateur ; détail jour par jour (semaine en cours par défaut)
    public ChargeMachineDTO charge(Long machineId, LocalDate du, LocalDate au) {
        MachineEntity machine = trouverOu404(machineRepository, machineId, "Machine");
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate debut = (du != null) ? du : lundi;
        LocalDate fin = (au != null) ? au : lundi.plusDays(6);
        if (debut.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
        }

        List<OrdreFabricationEntity> ordres = ordreFabricationRepository.findByMachineIdAndStatutNot(machineId, StatutOrdre.ANNULE);
        List<MaintenanceEntity> maintenances = maintenanceRepository.findByMachineIdOrderByDebutAsc(machineId);

        ChargeMachineDTO dto = new ChargeMachineDTO();
        dto.setMachineId(machine.getId());
        dto.setMachineCode(machine.getCode());
        dto.setDu(debut);
        dto.setAu(fin);
        dto.setNombreOrdres((int) ordres.stream()
                .filter(o -> o.chevauche(debut.atStartOfDay(), fin.plusDays(1).atStartOfDay()))
                .count());

        long minutesDisponibles = 0;
        long minutesOccupees = 0;
        for (LocalDate jour = debut; !jour.isAfter(fin); jour = jour.plusDays(1)) {
            long dispo = 0;
            long occupe = 0;
            if (jour.getDayOfWeek() != DayOfWeek.SATURDAY && jour.getDayOfWeek() != DayOfWeek.SUNDAY) {
                LocalDateTime ouverture = jour.atTime(OUVERTURE);
                LocalDateTime fermeture = jour.atTime(FERMETURE);
                long arret = maintenances.stream()
                        .mapToLong(m -> recouvrement(m.getDebut(), m.getFin(), ouverture, fermeture)).sum();
                dispo = Math.max(0, Duration.between(ouverture, fermeture).toMinutes() - arret);
                occupe = Math.min(dispo, ordres.stream()
                        .mapToLong(o -> recouvrement(o.getDatePrevue(), o.dateFinPrevue(), ouverture, fermeture)).sum());
            }
            ChargeMachineDTO.Jour ligne = new ChargeMachineDTO.Jour();
            ligne.setDate(jour);
            ligne.setHeuresDisponibles(enHeures(dispo));
            ligne.setHeuresOccupees(enHeures(occupe));
            ligne.setTauxUtilisation(taux(occupe, dispo));
            dto.getJours().add(ligne);
            minutesDisponibles += dispo;
            minutesOccupees += occupe;
        }
        dto.setHeuresDisponibles(enHeures(minutesDisponibles));
        dto.setHeuresOccupees(enHeures(minutesOccupees));
        dto.setTauxUtilisation(taux(minutesOccupees, minutesDisponibles));
        return dto;
    }

    // F19 - minutes communes aux intervalles [debut, fin[ et [debutFenetre, finFenetre[
    private long recouvrement(LocalDateTime debut, LocalDateTime fin, LocalDateTime debutFenetre, LocalDateTime finFenetre) {
        LocalDateTime d = debut.isAfter(debutFenetre) ? debut : debutFenetre;
        LocalDateTime f = fin.isBefore(finFenetre) ? fin : finFenetre;
        return f.isAfter(d) ? Duration.between(d, f).toMinutes() : 0;
    }

    // F19 - conversion minutes -> heures (2 décimales)
    private BigDecimal enHeures(long minutes) {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    // F19 - taux en % ; 0 % si aucune heure disponible
    private BigDecimal taux(long occupe, long disponible) {
        if (disponible == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(occupe * 100).divide(BigDecimal.valueOf(disponible), 2, RoundingMode.HALF_UP);
    }

    // F19 - Entity -> DTO
    private MaintenanceResponseDTO maintenanceVersDTO(MaintenanceEntity maintenance) {
        MaintenanceResponseDTO dto = new MaintenanceResponseDTO();
        dto.setId(maintenance.getId());
        dto.setMachineId(maintenance.getMachine().getId());
        dto.setDebut(maintenance.getDebut());
        dto.setFin(maintenance.getFin());
        dto.setMotif(maintenance.getMotif());
        return dto;
    }
}

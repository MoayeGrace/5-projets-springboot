package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.OrdreFabricationRequestDTO;
import com.projet3.atelierdeproduction.DTO.OrdreFabricationResponseDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.MachineEntity;
import com.projet3.atelierdeproduction.Entity.OperateurEntity;
import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.MachineRepository;
import com.projet3.atelierdeproduction.Repository.OperateurRepository;
import com.projet3.atelierdeproduction.Repository.OrdreFabricationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class OrdreFabricationService {

    private final OrdreFabricationRepository ordreFabricationRepository;
    private final ArticleRepository articleRepository;
    private final MachineRepository machineRepository;
    private final OperateurRepository operateurRepository;

    // F8 - GET /ordres-fabrication?statut= : ordres du statut demandé (tous si absent), triés par date prévue
    public List<OrdreFabricationResponseDTO> lister(StatutOrdre statut) {
        List<OrdreFabricationEntity> ordres = (statut == null)
                ? ordreFabricationRepository.findAllByOrderByDatePrevueAsc()
                : ordreFabricationRepository.findByStatutOrderByDatePrevueAsc(statut);
        return ordres.stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F9 - GET /machines/{id}/ordres?du=&au= : programme de la machine sur la période (semaine en cours par défaut),
    // trié par date prévue ; 404 si la machine n'existe pas, 400 si "du" est après "au"
    public List<OrdreFabricationResponseDTO> listerParMachine(Long machineId, LocalDate du, LocalDate au) {
        trouverOu404(machineRepository, machineId, "Machine");
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate debut = (du != null) ? du : lundi;
        LocalDate fin = (au != null) ? au : lundi.plusDays(6);
        if (debut.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
        }
        return ordreFabricationRepository
                .findByMachineIdAndDatePrevueGreaterThanEqualAndDatePrevueLessThanOrderByDatePrevueAsc(
                        machineId, debut.atStartOfDay(), fin.plusDays(1).atStartOfDay())
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /ordres-fabrication/{id} : récupère un ordre par son id (404 s'il n'existe pas)
    public OrdreFabricationResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication"));
    }

    // POST /ordres-fabrication : lance un nouvel ordre (INSERT) : article, quantité, date, machine et opérateur
    public OrdreFabricationResponseDTO creer(OrdreFabricationRequestDTO dto) {
        OrdreFabricationEntity ordre = new OrdreFabricationEntity();
        dtoVersEntity(dto, ordre);
        verifierMachineLibre(ordre);   // F12
        return entityVersResponseDTO(ordreFabricationRepository.save(ordre));
    }

    // PUT /ordres-fabrication/{id} : charge l'ordre existant (404 sinon), remplace ses champs (dont le statut), puis UPDATE
    public OrdreFabricationResponseDTO modifier(Long id, OrdreFabricationRequestDTO dto) {
        OrdreFabricationEntity ordre = trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication");
        dtoVersEntity(dto, ordre);
        verifierMachineLibre(ordre);   // F12 - nouvelle date, machine ou quantité : le créneau est revérifié
        return entityVersResponseDTO(ordreFabricationRepository.save(ordre));
    }

    // F12 - aucun autre ordre non annulé de la machine ne doit recouvrir le créneau de cet ordre (409 citant l'ordre bloquant)
    private void verifierMachineLibre(OrdreFabricationEntity ordre) {
        if (ordre.getStatut() == StatutOrdre.ANNULE) {
            return;
        }
        LocalDateTime debut = ordre.getDatePrevue();
        LocalDateTime fin = ordre.dateFinPrevue();
        for (OrdreFabricationEntity autre : ordreFabricationRepository.findByMachineIdAndStatutNot(ordre.getMachine().getId(), StatutOrdre.ANNULE)) {
            if (!autre.getId().equals(ordre.getId()) && autre.chevauche(debut, fin)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La machine " + ordre.getMachine().getCode() + " est déjà occupée par l'ordre " + autre.getId()
                                + " du " + autre.getDatePrevue() + " au " + autre.dateFinPrevue());
            }
        }
    }

    // DELETE /ordres-fabrication/{id} : vérifie que l'ordre existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        ordreFabricationRepository.delete(trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication"));
    }

    // DTO -> Entity (ids transformés en objets, 404 s'ils n'existent pas ; statut absent = inchangé)
    private void dtoVersEntity(OrdreFabricationRequestDTO dto, OrdreFabricationEntity ordre) {
        ArticleEntity article = trouverOu404(articleRepository, dto.getArticleId(), "Article");
        MachineEntity machine = trouverOu404(machineRepository, dto.getMachineId(), "Machine");
        OperateurEntity operateur = trouverOu404(operateurRepository, dto.getOperateurId(), "Opérateur");

        ordre.setArticle(article);
        ordre.setQuantite(dto.getQuantite());
        ordre.setDatePrevue(dto.getDatePrevue());
        if (dto.getStatut() != null) {
            ordre.setStatut(dto.getStatut());
        }
        ordre.setMachine(machine);
        ordre.setOperateur(operateur);
    }

    // Entity -> DTO
    private OrdreFabricationResponseDTO entityVersResponseDTO(OrdreFabricationEntity ordre) {
        OrdreFabricationResponseDTO dto = new OrdreFabricationResponseDTO();
        dto.setId(ordre.getId());
        dto.setArticleId(ordre.getArticle().getId());
        dto.setArticleReference(ordre.getArticle().getReference());
        dto.setArticleLibelle(ordre.getArticle().getLibelle());
        dto.setQuantite(ordre.getQuantite());
        dto.setDatePrevue(ordre.getDatePrevue());
        dto.setDateFinPrevue(ordre.dateFinPrevue());   // F12
        dto.setStatut(ordre.getStatut());
        dto.setMachineId(ordre.getMachine().getId());
        dto.setMachineCode(ordre.getMachine().getCode());
        dto.setOperateurId(ordre.getOperateur().getId());
        dto.setOperateurNom(ordre.getOperateur().getNom());
        return dto;
    }
}

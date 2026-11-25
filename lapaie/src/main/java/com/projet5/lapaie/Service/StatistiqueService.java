package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.CumulAnnuelDTO;
import com.projet5.lapaie.DTO.MasseSalarialeDTO;
import com.projet5.lapaie.DTO.SoldeCongesDTO;
import com.projet5.lapaie.Entity.AbsenceEntity;
import com.projet5.lapaie.Entity.BulletinEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Entity.LigneBulletinEntity;
import com.projet5.lapaie.Entity.TypeAbsence;
import com.projet5.lapaie.Repository.AbsenceRepository;
import com.projet5.lapaie.Repository.BulletinRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    // F18 - jours de congé acquis par mois complet travaillé
    private static final BigDecimal JOURS_ACQUIS_PAR_MOIS = new BigDecimal("2.5");

    private final BulletinRepository bulletinRepository;
    private final EmployeRepository employeRepository;   // F18
    private final AbsenceRepository absenceRepository;   // F18

    // F17 - GET /bulletins/masse-salariale?mois= : somme des bruts, retenues et nets du mois (en cours par défaut),
    // par service et au total ; les bulletins non édités sont inclus mais signalés comme provisoires
    public MasseSalarialeDTO masseSalariale(String mois) {
        String periode = (mois != null) ? verifierFormat(mois) : YearMonth.now().toString();
        MasseSalarialeDTO dto = new MasseSalarialeDTO();
        dto.setMois(periode);

        Map<String, MasseSalarialeDTO.Totaux> parService = new TreeMap<>();
        for (BulletinEntity bulletin : bulletinRepository.findByMois(periode)) {
            String service = bulletin.getEmploye().getService();
            MasseSalarialeDTO.Totaux totaux = parService.computeIfAbsent(service, s -> {
                MasseSalarialeDTO.Totaux t = new MasseSalarialeDTO.Totaux();
                t.setService(s);
                return t;
            });
            ajouter(totaux, bulletin);
            ajouter(dto.getTotal(), bulletin);
        }
        dto.getServices().addAll(parService.values());
        return dto;
    }

    // F18 - GET /employes/{id}/solde-conges?date= : jours acquis (2,5 par mois complet depuis l'embauche) moins jours de
    // congé payé consommés jusqu'à la date (aujourd'hui par défaut) ; un solde négatif est renvoyé tel quel et signalé
    public SoldeCongesDTO soldeConges(Long employeId, LocalDate date) {
        EmployeEntity employe = trouverOu404(employeRepository, employeId, "Employé");
        LocalDate jour = (date != null) ? date : LocalDate.now();

        long mois = Math.max(0, ChronoUnit.MONTHS.between(employe.getDateEmbauche(), jour));
        BigDecimal acquis = JOURS_ACQUIS_PAR_MOIS.multiply(BigDecimal.valueOf(mois));
        long consommes = 0;
        for (AbsenceEntity absence : absenceRepository.findByEmployeId(employeId)) {
            if (absence.getType() == TypeAbsence.CONGE_PAYE && !absence.getDateDebut().isAfter(jour)) {
                LocalDate fin = absence.getDateFin().isAfter(jour) ? jour : absence.getDateFin();
                consommes += ChronoUnit.DAYS.between(absence.getDateDebut(), fin) + 1;
            }
        }

        SoldeCongesDTO dto = new SoldeCongesDTO();
        dto.setEmployeId(employe.getId());
        dto.setEmployeNom(employe.getNom());
        dto.setDate(jour);
        dto.setMoisTravailles(mois);
        dto.setJoursAcquis(acquis);
        dto.setJoursConsommes(consommes);
        dto.setSolde(acquis.subtract(BigDecimal.valueOf(consommes)));
        dto.setSoldeNegatif(dto.getSolde().signum() < 0);
        return dto;
    }

    // F19 - GET /employes/{id}/cumul?annee= : brut, retenues et net mois par mois (régularisations incluses), total annuel
    // et détail par rubrique ; les mois sans bulletin sont à zéro, un employé embauché dans l'année part de son premier bulletin
    public CumulAnnuelDTO cumulAnnuel(Long employeId, Integer annee) {
        EmployeEntity employe = trouverOu404(employeRepository, employeId, "Employé");
        int an = (annee != null) ? annee : LocalDate.now().getYear();
        List<BulletinEntity> bulletins = bulletinRepository.findByEmployeIdAndMoisStartingWithOrderByMoisDesc(employeId, an + "-");

        CumulAnnuelDTO dto = new CumulAnnuelDTO();
        dto.setEmployeId(employe.getId());
        dto.setEmployeNom(employe.getNom());
        dto.setAnnee(an);

        int premierMois = 1;
        if (employe.getDateEmbauche().getYear() == an) {
            premierMois = bulletins.stream()
                    .mapToInt(b -> YearMonth.parse(b.getMois()).getMonthValue())
                    .min()
                    .orElse(employe.getDateEmbauche().getMonthValue());
        }
        Map<String, CumulAnnuelDTO.ParRubrique> parRubrique = new LinkedHashMap<>();
        for (int m = premierMois; m <= 12; m++) {
            CumulAnnuelDTO.Montants ligne = new CumulAnnuelDTO.Montants();
            ligne.setMois(YearMonth.of(an, m).toString());
            for (BulletinEntity bulletin : bulletins) {
                if (bulletin.getMois().equals(ligne.getMois())) {
                    ligne.ajouter(bulletin.getBrut(), bulletin.getRetenues(), bulletin.getNet());
                    cumulerRubriques(bulletin, parRubrique);
                }
            }
            dto.getMois().add(ligne);
            dto.getTotal().ajouter(ligne.getBrut(), ligne.getRetenues(), ligne.getNet());
        }
        dto.getRubriques().addAll(parRubrique.values());
        return dto;
    }

    // F19 - ajoute chaque ligne du bulletin au cumul de sa rubrique
    private void cumulerRubriques(BulletinEntity bulletin, Map<String, CumulAnnuelDTO.ParRubrique> parRubrique) {
        for (LigneBulletinEntity ligne : bulletin.getLignes()) {
            CumulAnnuelDTO.ParRubrique cumul = parRubrique.computeIfAbsent(ligne.getCode(), code -> {
                CumulAnnuelDTO.ParRubrique nouveau = new CumulAnnuelDTO.ParRubrique();
                nouveau.setCode(code);
                nouveau.setLibelle(ligne.getCode().equals("REGULARISATION") ? "Régularisations" : ligne.getLibelle());
                nouveau.setType(ligne.getType());
                return nouveau;
            });
            cumul.setTotal(cumul.getTotal().add(ligne.getMontant()));
        }
    }

    // F17 - cumule un bulletin dans des totaux
    private void ajouter(MasseSalarialeDTO.Totaux totaux, BulletinEntity bulletin) {
        totaux.setBrut(totaux.getBrut().add(bulletin.getBrut()));
        totaux.setRetenues(totaux.getRetenues().add(bulletin.getRetenues()));
        totaux.setNet(totaux.getNet().add(bulletin.getNet()));
        totaux.setEffectif(totaux.getEffectif() + 1);
        if (!bulletin.getEdite()) {
            totaux.setBulletinsProvisoires(totaux.getBulletinsProvisoires() + 1);
            totaux.setProvisoire(true);
        }
    }

    // Vérifie le format AAAA-MM (400 sinon)
    private String verifierFormat(String mois) {
        if (!mois.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mois doit être au format AAAA-MM");
        }
        return mois;
    }
}

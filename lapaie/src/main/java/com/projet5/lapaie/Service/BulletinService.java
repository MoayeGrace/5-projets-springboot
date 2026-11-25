package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.BulletinRequestDTO;
import com.projet5.lapaie.DTO.BulletinResponseDTO;
import com.projet5.lapaie.DTO.LigneBulletinDTO;
import com.projet5.lapaie.DTO.PageResponseDTO;
import com.projet5.lapaie.DTO.RegularisationRequestDTO;
import com.projet5.lapaie.Entity.AbsenceEntity;
import com.projet5.lapaie.Entity.BulletinEntity;
import com.projet5.lapaie.Entity.ContratEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Entity.EtatBulletin;
import com.projet5.lapaie.Entity.LigneBulletinEntity;
import com.projet5.lapaie.Entity.RubriqueEntity;
import com.projet5.lapaie.Entity.TypeLigne;
import com.projet5.lapaie.Entity.TypeRubrique;
import com.projet5.lapaie.Repository.AbsenceRepository;
import com.projet5.lapaie.Repository.BulletinRepository;
import com.projet5.lapaie.Repository.ContratRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import com.projet5.lapaie.Repository.RubriqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class BulletinService {

    private final BulletinRepository bulletinRepository;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;     // F13
    private final AbsenceRepository absenceRepository;     // F13
    private final RubriqueRepository rubriqueRepository;   // F13
    private final ClotureService clotureService;           // F16

    // F10 - taille de page maximale autorisée
    private static final int TAILLE_PAGE_MAX = 200;

    // F13 - mois de paie conventionnel de 30 jours pour le prorata des absences
    private static final BigDecimal JOURS_PAR_MOIS = BigDecimal.valueOf(30);

    // F10 - GET /bulletins?page=&size=&sort= : bulletins page par page (une taille > 200 est ramenée à 200)
    public PageResponseDTO<BulletinResponseDTO> lister(Pageable pageable) {
        if (pageable.getPageSize() > TAILLE_PAGE_MAX) {
            pageable = PageRequest.of(pageable.getPageNumber(), TAILLE_PAGE_MAX, pageable.getSort());
        }
        return new PageResponseDTO<>(bulletinRepository.findAll(pageable).map(this::entityVersResponseDTO));
    }

    // GET /bulletins/{id} : récupère un bulletin par son id (404 si introuvable)
    public BulletinResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(bulletinRepository, id, "Bulletin"));
    }

    // F7 - GET /employes/{id}/bulletins?annee= : bulletins de l'année (en cours par défaut), du mois le plus récent au plus ancien
    public List<BulletinResponseDTO> listerParEmploye(Long employeId, Integer annee) {
        trouverOu404(employeRepository, employeId, "Employé");
        int an = (annee != null) ? annee : LocalDate.now().getYear();
        return bulletinRepository.findByEmployeIdAndMoisStartingWithOrderByMoisDesc(employeId, an + "-")
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F13 - POST /bulletins : calcule le bulletin de l'employé pour le mois, sans saisie manuelle.
    // 409 s'il existe déjà (ou si le mois est clôturé), 400 si aucun contrat n'est actif ce mois-là
    @Transactional
    public BulletinResponseDTO creer(BulletinRequestDTO dto) {
        EmployeEntity employe = trouverOu404(employeRepository, dto.getEmployeId(), "Employé");
        clotureService.verifierMoisOuvert(dto.getMois());   // F16
        YearMonth mois = YearMonth.parse(dto.getMois());
        if (bulletinRepository.existsByEmployeIdAndMois(employe.getId(), dto.getMois())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un bulletin existe déjà pour " + employe.getNom() + " en " + dto.getMois());
        }
        ContratEntity contrat = contratActif(employe.getId(), mois);

        BulletinEntity bulletin = new BulletinEntity();
        bulletin.setEmploye(employe);
        bulletin.setMois(dto.getMois());
        calculer(bulletin, contrat, mois);
        return entityVersResponseDTO(bulletinRepository.save(bulletin));
    }

    // F15 - POST /bulletins/{id}/regularisation : corrige un bulletin édité sans le toucher ; une ligne de régularisation
    // (mois corrigé et motif) est ajoutée au prochain bulletin non édité de l'employé, dont le net est ajusté.
    // 409 si le bulletin n'est pas édité ou s'il n'existe pas encore de bulletin en brouillon pour porter la correction
    @Transactional
    public BulletinResponseDTO regulariser(Long id, RegularisationRequestDTO dto) {
        BulletinEntity origine = trouverOu404(bulletinRepository, id, "Bulletin");
        if (origine.getEtat() == EtatBulletin.BROUILLON) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le bulletin " + id + " n'est pas édité : corrigez-le directement au lieu de le régulariser");
        }
        BulletinEntity cible = bulletinRepository.findFirstByEmployeIdAndMoisGreaterThanAndEtatOrderByMoisAsc(
                        origine.getEmploye().getId(), origine.getMois(), EtatBulletin.BROUILLON)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Aucun bulletin en brouillon après " + origine.getMois() + " pour porter la régularisation : "
                                + "calculez d'abord le bulletin suivant"));

        LigneBulletinEntity ligne = ajouterLigne(cible, TypeLigne.REGULARISATION, null,
                "Régularisation " + origine.getMois(), null, null, dto.getMontant());
        ligne.setMoisCorrige(origine.getMois());   // référence au bulletin corrigé : même employé, ce mois
        ligne.setMotif(dto.getMotif());
        cible.setNet(cible.getNet().add(dto.getMontant()));
        return entityVersResponseDTO(bulletinRepository.save(cible));
    }

    // F13 - le contrat qui couvre au moins un jour du mois (le plus récent s'il y en a plusieurs) ; 400 s'il n'y en a aucun
    private ContratEntity contratActif(Long employeId, YearMonth mois) {
        return contratRepository.findByEmployeId(employeId).stream()
                .filter(c -> c.chevauche(mois.atDay(1), mois.atEndOfMonth()))
                .max(Comparator.comparing(ContratEntity::getDateDebut))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aucun contrat actif pour cet employé en " + mois));
    }

    // F13 - calcul ligne par ligne, rubriques dans leur ordre de calcul : BASE et PRIME s'appliquent au salaire de base
    // du contrat et s'ajoutent au brut ; avant la première cotisation, les absences non rémunérées réduisent le brut
    // au prorata des jours ; chaque COTISATION s'applique au brut obtenu par les lignes précédentes. Net = brut - retenues
    private void calculer(BulletinEntity bulletin, ContratEntity contrat, YearMonth mois) {
        long joursNonPayes = joursNonRemuneres(bulletin.getEmploye().getId(), mois);
        boolean absenceAppliquee = false;
        for (RubriqueEntity rubrique : rubriqueRepository.findAllByOrderByOrdreCalculAscCodeAsc()) {
            if (rubrique.getType() == TypeRubrique.COTISATION && !absenceAppliquee) {
                appliquerAbsences(bulletin, joursNonPayes);
                absenceAppliquee = true;
            }
            BigDecimal base = (rubrique.getType() == TypeRubrique.COTISATION) ? bulletin.getBrut() : contrat.getSalaireBase();
            BigDecimal montant = base.multiply(rubrique.getTaux()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (rubrique.getType() == TypeRubrique.COTISATION) {
                bulletin.setRetenues(bulletin.getRetenues().add(montant));
            } else {
                bulletin.setBrut(bulletin.getBrut().add(montant));
            }
            // F14 - le libellé et le taux de la rubrique sont copiés dans la ligne : ils restent figés avec le bulletin
            ajouterLigne(bulletin, TypeLigne.valueOf(rubrique.getType().name()), rubrique, rubrique.getLibelle(),
                    base, rubrique.getTaux(), montant);
        }
        if (!absenceAppliquee) {
            appliquerAbsences(bulletin, joursNonPayes);
        }
        bulletin.setNet(bulletin.getBrut().subtract(bulletin.getRetenues()));
    }

    // F13 - réduction du brut pour absence non rémunérée : brut × jours / 30
    private void appliquerAbsences(BulletinEntity bulletin, long joursNonPayes) {
        if (joursNonPayes == 0) {
            return;
        }
        BigDecimal reduction = bulletin.getBrut().multiply(BigDecimal.valueOf(joursNonPayes))
                .divide(JOURS_PAR_MOIS, 2, RoundingMode.HALF_UP);
        ajouterLigne(bulletin, TypeLigne.ABSENCE, null, "Absence non rémunérée (" + joursNonPayes + " jours)",
                bulletin.getBrut(), null, reduction.negate());
        bulletin.setBrut(bulletin.getBrut().subtract(reduction));
    }

    // F13 - jours d'absence non rémunérée de l'employé compris dans le mois
    private long joursNonRemuneres(Long employeId, YearMonth mois) {
        LocalDate debutMois = mois.atDay(1);
        LocalDate finMois = mois.atEndOfMonth();
        long jours = 0;
        for (AbsenceEntity absence : absenceRepository.findByEmployeId(employeId)) {
            if (!absence.getRemuneree() && absence.chevauche(debutMois, finMois)) {
                LocalDate debut = absence.getDateDebut().isBefore(debutMois) ? debutMois : absence.getDateDebut();
                LocalDate fin = absence.getDateFin().isAfter(finMois) ? finMois : absence.getDateFin();
                jours += ChronoUnit.DAYS.between(debut, fin) + 1;
            }
        }
        return jours;
    }

    // F13 - ajoute une ligne au bulletin, à la suite des précédentes
    private LigneBulletinEntity ajouterLigne(BulletinEntity bulletin, TypeLigne type, RubriqueEntity rubrique, String libelle,
                                             BigDecimal base, BigDecimal taux, BigDecimal montant) {
        LigneBulletinEntity ligne = new LigneBulletinEntity();
        ligne.setBulletin(bulletin);
        ligne.setOrdre(bulletin.getLignes().size() + 1);
        ligne.setType(type);
        ligne.setRubrique(rubrique);
        ligne.setLibelle(libelle);
        ligne.setBase(base);
        ligne.setTaux(taux);
        ligne.setMontant(montant);
        bulletin.getLignes().add(ligne);
        return ligne;
    }

    // F14 - POST /bulletins/{id}/edition : fige le bulletin remis à l'employé ; il doit être complet (calculé) et son net
    // positif (400), et ne pas être déjà édité (409)
    public BulletinResponseDTO editer(Long id) {
        BulletinEntity bulletin = trouverOu404(bulletinRepository, id, "Bulletin");
        if (bulletin.getEtat() != EtatBulletin.BROUILLON) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le bulletin " + id + " est déjà édité (" + bulletin.getEtat() + ")");
        }
        if (bulletin.getLignes().isEmpty() || bulletin.getNet().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le bulletin " + id + " est incomplet : il doit être calculé et son net doit être positif");
        }
        bulletin.setEtat(EtatBulletin.EDITE);
        return entityVersResponseDTO(bulletinRepository.save(bulletin));
    }

    // F14 / F16 - seul un bulletin en BROUILLON, sur un mois ouvert, se modifie ou se supprime (409)
    private void verifierBrouillon(BulletinEntity bulletin) {
        if (bulletin.getEtat() != EtatBulletin.BROUILLON) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le bulletin " + bulletin.getId() + " est " + bulletin.getEtat() + " : il ne peut plus être modifié ni supprimé");
        }
        clotureService.verifierMoisOuvert(bulletin.getMois());
    }

    // PUT /bulletins/{id} : corrige le brut et/ou le net d'un bulletin avant édition (404 si introuvable, 409 si édité)
    public BulletinResponseDTO modifier(Long id, BulletinRequestDTO dto) {
        BulletinEntity bulletin = trouverOu404(bulletinRepository, id, "Bulletin");
        verifierBrouillon(bulletin);   // F14
        if (dto.getBrut() != null) {
            bulletin.setBrut(dto.getBrut());
        }
        if (dto.getNet() != null) {
            bulletin.setNet(dto.getNet());
        }
        return entityVersResponseDTO(bulletinRepository.save(bulletin));
    }

    // DELETE /bulletins/{id} : vérifie que le bulletin existe (404 sinon) et n'est pas édité (409), puis le supprime
    public void supprimer(Long id) {
        BulletinEntity bulletin = trouverOu404(bulletinRepository, id, "Bulletin");
        verifierBrouillon(bulletin);   // F14
        bulletinRepository.delete(bulletin);
    }

    // Entity -> DTO (avec le détail des lignes)
    private BulletinResponseDTO entityVersResponseDTO(BulletinEntity bulletin) {
        BulletinResponseDTO dto = new BulletinResponseDTO();
        dto.setId(bulletin.getId());
        dto.setEmployeId(bulletin.getEmploye().getId());
        dto.setEmployeNom(bulletin.getEmploye().getNom());
        dto.setMois(bulletin.getMois());
        dto.setBrut(bulletin.getBrut());
        dto.setRetenues(bulletin.getRetenues());   // F13
        dto.setNet(bulletin.getNet());
        dto.setEtat(bulletin.getEtat());           // F7 / F14 / F16
        dto.setDateModification(bulletin.getDateModification());
        dto.setLignes(bulletin.getLignes().stream().map(this::ligneVersDTO).toList());   // F13
        return dto;
    }

    // F13 - Entity -> DTO pour une ligne du bulletin
    private LigneBulletinDTO ligneVersDTO(LigneBulletinEntity ligne) {
        LigneBulletinDTO dto = new LigneBulletinDTO();
        dto.setOrdre(ligne.getOrdre());
        dto.setType(ligne.getType());
        dto.setRubriqueCode(ligne.getRubrique() != null ? ligne.getRubrique().getCode() : null);
        dto.setLibelle(ligne.getLibelle());
        dto.setBase(ligne.getBase());
        dto.setTaux(ligne.getTaux());
        dto.setMontant(ligne.getMontant());
        dto.setMoisCorrige(ligne.getMoisCorrige());   // F15
        dto.setMotif(ligne.getMotif());
        return dto;
    }
}

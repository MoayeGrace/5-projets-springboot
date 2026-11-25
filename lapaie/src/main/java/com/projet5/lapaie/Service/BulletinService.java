package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.BulletinRequestDTO;
import com.projet5.lapaie.DTO.BulletinResponseDTO;
import com.projet5.lapaie.DTO.LigneBulletinDTO;
import com.projet5.lapaie.DTO.PageResponseDTO;
import com.projet5.lapaie.DTO.RegularisationRequestDTO;
import com.projet5.lapaie.DTO.RegularisationResponseDTO;
import com.projet5.lapaie.Entity.AbsenceEntity;
import com.projet5.lapaie.Entity.Assiette;
import com.projet5.lapaie.Entity.BulletinEntity;
import com.projet5.lapaie.Entity.ContratEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Entity.LigneBulletinEntity;
import com.projet5.lapaie.Entity.RegularisationEntity;
import com.projet5.lapaie.Entity.RubriqueEntity;
import com.projet5.lapaie.Entity.TypeAbsence;
import com.projet5.lapaie.Entity.TypeRubrique;
import com.projet5.lapaie.Repository.AbsenceRepository;
import com.projet5.lapaie.Repository.BulletinRepository;
import com.projet5.lapaie.Repository.ContratRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import com.projet5.lapaie.Repository.RegularisationRepository;
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
import java.time.LocalDateTime;
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
    private final RegularisationRepository regularisationRepository;   // F15

    // F10 - taille de page maximale autorisée
    private static final int TAILLE_PAGE_MAX = 200;

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
    // 409 s'il existe déjà, 400 si aucun contrat n'est actif ce mois-là
    @Transactional
    public BulletinResponseDTO creer(BulletinRequestDTO dto) {
        EmployeEntity employe = trouverOu404(employeRepository, dto.getEmployeId(), "Employé");
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
        bulletinRepository.save(bulletin);

        // F15 - les régularisations en attente sur un mois antérieur sont portées sur ce nouveau bulletin
        for (RegularisationEntity regularisation : regularisationRepository
                .findByBulletinOrigineEmployeIdAndBulletinCibleIsNullOrderByIdAsc(employe.getId())) {
            if (regularisation.getBulletinOrigine().getMois().compareTo(bulletin.getMois()) < 0) {
                appliquerRegularisation(regularisation, bulletin);
            }
        }
        return entityVersResponseDTO(bulletin);
    }

    // F15 - POST /bulletins/{id}/regularisation : corrige un bulletin édité sans le toucher ; la régularisation est portée
    // sur le prochain bulletin non édité de l'employé (ou mise en attente s'il n'existe pas encore). 409 si non édité
    @Transactional
    public RegularisationResponseDTO regulariser(Long id, RegularisationRequestDTO dto) {
        BulletinEntity origine = trouverOu404(bulletinRepository, id, "Bulletin");
        if (!origine.getEdite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le bulletin " + id + " n'est pas édité : corrigez-le directement au lieu de le régulariser");
        }
        RegularisationEntity regularisation = new RegularisationEntity();
        regularisation.setBulletinOrigine(origine);
        regularisation.setMontant(dto.getMontant());
        regularisation.setMotif(dto.getMotif());
        regularisationRepository.save(regularisation);

        bulletinRepository.findFirstByEmployeIdAndMoisGreaterThanAndEditeFalseOrderByMoisAsc(
                        origine.getEmploye().getId(), origine.getMois())
                .ifPresent(suivant -> appliquerRegularisation(regularisation, suivant));
        return regularisationVersDTO(regularisation);
    }

    // F15 - ajoute la ligne de régularisation (avec la référence au bulletin corrigé) et met à jour brut et net
    private void appliquerRegularisation(RegularisationEntity regularisation, BulletinEntity cible) {
        ajouterLigne(cible, "REGULARISATION",
                "Régularisation du bulletin " + regularisation.getBulletinOrigine().getMois()
                        + " (n°" + regularisation.getBulletinOrigine().getId() + ") : " + regularisation.getMotif(),
                TypeRubrique.GAIN, null, null, regularisation.getMontant());
        cible.setBrut(cible.getBrut().add(regularisation.getMontant()));
        cible.setNet(cible.getNet().add(regularisation.getMontant()));
        regularisation.setBulletinCible(cible);
    }

    // F15 - Entity -> DTO
    private RegularisationResponseDTO regularisationVersDTO(RegularisationEntity regularisation) {
        RegularisationResponseDTO dto = new RegularisationResponseDTO();
        dto.setId(regularisation.getId());
        dto.setBulletinOrigineId(regularisation.getBulletinOrigine().getId());
        dto.setMoisOrigine(regularisation.getBulletinOrigine().getMois());
        dto.setMontant(regularisation.getMontant());
        dto.setMotif(regularisation.getMotif());
        dto.setEnAttente(regularisation.getBulletinCible() == null);
        if (regularisation.getBulletinCible() != null) {
            dto.setBulletinCibleId(regularisation.getBulletinCible().getId());
            dto.setMoisCible(regularisation.getBulletinCible().getMois());
        }
        return dto;
    }

    // F13 - le contrat qui couvre au moins un jour du mois (le plus récent s'il y en a plusieurs) ; 400 s'il n'y en a aucun
    private ContratEntity contratActif(Long employeId, YearMonth mois) {
        return contratRepository.findByEmployeId(employeId).stream()
                .filter(c -> c.chevauche(mois.atDay(1), mois.atEndOfMonth()))
                .max(Comparator.comparing(ContratEntity::getDateDebut))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aucun contrat actif pour cet employé en " + mois));
    }

    // F13 - calcul ligne par ligne : salaire de base du contrat, réduction pour absences non rémunérées au prorata
    // des jours, puis rubriques dans leur ordre de calcul (gains -> brut, retenues -> retenues) ; net = brut - retenues
    private void calculer(BulletinEntity bulletin, ContratEntity contrat, YearMonth mois) {
        BigDecimal base = contrat.getSalaireBase();
        BigDecimal brut = base;
        BigDecimal retenues = BigDecimal.ZERO;
        ajouterLigne(bulletin, "SALAIRE_BASE", "Salaire de base (" + contrat.getPoste() + ")", TypeRubrique.GAIN, null, null, base);

        long joursNonPayes = joursSansSolde(bulletin.getEmploye().getId(), mois);
        if (joursNonPayes > 0) {
            BigDecimal reduction = base.multiply(BigDecimal.valueOf(joursNonPayes))
                    .divide(BigDecimal.valueOf(mois.lengthOfMonth()), 2, RoundingMode.HALF_UP);
            brut = brut.subtract(reduction);
            ajouterLigne(bulletin, "ABSENCE", "Absence non rémunérée (" + joursNonPayes + " j)", TypeRubrique.GAIN,
                    null, null, reduction.negate());
        }

        for (RubriqueEntity rubrique : rubriqueRepository.findAllByOrderByOrdreCalculAscCodeAsc()) {
            BigDecimal assiette = (rubrique.getAssiette() == Assiette.BRUT) ? brut : base;   // dépend des lignes précédentes
            BigDecimal montant = assiette.multiply(rubrique.getTaux())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (rubrique.getType() == TypeRubrique.GAIN) {
                brut = brut.add(montant);
            } else {
                retenues = retenues.add(montant);
            }
            ajouterLigne(bulletin, rubrique.getCode(), rubrique.getLibelle(), rubrique.getType(), assiette, rubrique.getTaux(), montant);
        }

        bulletin.setBrut(brut);
        bulletin.setRetenues(retenues);
        bulletin.setNet(brut.subtract(retenues));
    }

    // F13 - jours d'absence SANS_SOLDE de l'employé compris dans le mois
    private long joursSansSolde(Long employeId, YearMonth mois) {
        LocalDate debutMois = mois.atDay(1);
        LocalDate finMois = mois.atEndOfMonth();
        long jours = 0;
        for (AbsenceEntity absence : absenceRepository.findByEmployeId(employeId)) {
            if (absence.getType() == TypeAbsence.SANS_SOLDE && absence.chevauche(debutMois, finMois)) {
                LocalDate debut = absence.getDateDebut().isBefore(debutMois) ? debutMois : absence.getDateDebut();
                LocalDate fin = absence.getDateFin().isAfter(finMois) ? finMois : absence.getDateFin();
                jours += ChronoUnit.DAYS.between(debut, fin) + 1;
            }
        }
        return jours;
    }

    // F13 - ajoute une ligne au bulletin, à la suite des précédentes
    private void ajouterLigne(BulletinEntity bulletin, String code, String libelle, TypeRubrique type,
                              BigDecimal base, BigDecimal taux, BigDecimal montant) {
        LigneBulletinEntity ligne = new LigneBulletinEntity();
        ligne.setBulletin(bulletin);
        ligne.setOrdre(bulletin.getLignes().size() + 1);
        ligne.setCode(code);
        ligne.setLibelle(libelle);
        ligne.setType(type);
        ligne.setBase(base);
        ligne.setTaux(taux);
        ligne.setMontant(montant);
        bulletin.getLignes().add(ligne);
    }

    // F14 - POST /bulletins/{id}/edition : fige le bulletin remis à l'employé ; il doit être complet (calculé) et son net
    // positif (400), et ne pas être déjà édité (409). Les taux sont déjà figés dans ses lignes
    public BulletinResponseDTO editer(Long id) {
        BulletinEntity bulletin = trouverOu404(bulletinRepository, id, "Bulletin");
        if (bulletin.getEdite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le bulletin " + id + " est déjà édité");
        }
        if (bulletin.getLignes().isEmpty() || bulletin.getNet().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le bulletin " + id + " est incomplet : il doit être calculé et son net doit être positif");
        }
        bulletin.setEdite(true);
        bulletin.setDateEdition(LocalDateTime.now());
        return entityVersResponseDTO(bulletinRepository.save(bulletin));
    }

    // F14 - un bulletin édité n'est plus modifiable ni supprimable (409)
    private void verifierNonEdite(BulletinEntity bulletin) {
        if (bulletin.getEdite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le bulletin " + bulletin.getId() + " est édité : il ne peut plus être modifié ni supprimé");
        }
    }

    // PUT /bulletins/{id} : corrige le brut et/ou le net d'un bulletin avant édition (404 si introuvable, 409 si édité)
    public BulletinResponseDTO modifier(Long id, BulletinRequestDTO dto) {
        BulletinEntity bulletin = trouverOu404(bulletinRepository, id, "Bulletin");
        verifierNonEdite(bulletin);   // F14
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
        verifierNonEdite(bulletin);   // F14
        bulletinRepository.delete(bulletin);
    }

    // Entity -> DTO (avec le détail des lignes)
    private BulletinResponseDTO entityVersResponseDTO(BulletinEntity bulletin) {
        BulletinResponseDTO dto = new BulletinResponseDTO();
        dto.setId(bulletin.getId());
        dto.setMois(bulletin.getMois());
        dto.setBrut(bulletin.getBrut());
        dto.setNet(bulletin.getNet());
        dto.setRetenues(bulletin.getRetenues());   // F13
        dto.setEdite(bulletin.getEdite());
        dto.setDateEdition(bulletin.getDateEdition());   // F14
        dto.setEmployeId(bulletin.getEmploye().getId());
        dto.setEmployeNom(bulletin.getEmploye().getNom());
        dto.setLignes(bulletin.getLignes().stream().map(this::ligneVersDTO).toList());   // F13
        return dto;
    }

    // F13 - Entity -> DTO pour une ligne du bulletin
    private LigneBulletinDTO ligneVersDTO(LigneBulletinEntity ligne) {
        LigneBulletinDTO dto = new LigneBulletinDTO();
        dto.setOrdre(ligne.getOrdre());
        dto.setCode(ligne.getCode());
        dto.setLibelle(ligne.getLibelle());
        dto.setType(ligne.getType());
        dto.setBase(ligne.getBase());
        dto.setTaux(ligne.getTaux());
        dto.setMontant(ligne.getMontant());
        return dto;
    }
}

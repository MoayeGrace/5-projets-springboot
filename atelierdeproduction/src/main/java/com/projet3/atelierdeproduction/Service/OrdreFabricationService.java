package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.BesoinsOrdreDTO;
import com.projet3.atelierdeproduction.DTO.OrdreFabricationRequestDTO;
import com.projet3.atelierdeproduction.DTO.OrdreFabricationResponseDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.MachineEntity;
import com.projet3.atelierdeproduction.Entity.OperateurEntity;
import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import com.projet3.atelierdeproduction.Entity.TypeArticle;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.MachineRepository;
import com.projet3.atelierdeproduction.Repository.OperateurRepository;
import com.projet3.atelierdeproduction.Repository.OrdreFabricationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class OrdreFabricationService {

    private final OrdreFabricationRepository ordreFabricationRepository;
    private final ArticleRepository articleRepository;
    private final MachineRepository machineRepository;
    private final OperateurRepository operateurRepository;
    private final DeploiementService deploiementService;                   // F14 / F18
    private final VersionNomenclatureService versionNomenclatureService;   // F15

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

    // POST /ordres-fabrication : crée un ordre PLANIFIE (INSERT) : article, quantité, date, machine et opérateur
    public OrdreFabricationResponseDTO creer(OrdreFabricationRequestDTO dto) {
        OrdreFabricationEntity ordre = new OrdreFabricationEntity();
        dtoVersEntity(dto, ordre);
        verifierMachineLibre(ordre);   // F12
        return entityVersResponseDTO(ordreFabricationRepository.saveAndFlush(ordre));   // flush : date de modification à jour
    }

    // PUT /ordres-fabrication/{id} : seul un ordre PLANIFIE se modifie (409 sinon) ; le créneau est revérifié (F12)
    public OrdreFabricationResponseDTO modifier(Long id, OrdreFabricationRequestDTO dto) {
        OrdreFabricationEntity ordre = trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication");
        if (ordre.getStatut() != StatutOrdre.PLANIFIE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seul un ordre PLANIFIE peut être modifié (statut actuel : " + ordre.getStatut() + ")");
        }
        dtoVersEntity(dto, ordre);
        verifierMachineLibre(ordre);
        return entityVersResponseDTO(ordreFabricationRepository.saveAndFlush(ordre));
    }

    // F12 - le créneau ne doit recouvrir ni l'arrêt de maintenance de la machine, ni un autre ordre non annulé (409)
    private void verifierMachineLibre(OrdreFabricationEntity ordre) {
        MachineEntity machine = ordre.getMachine();
        if (machine.getDebutMaintenance() != null && ordre.chevauche(machine.getDebutMaintenance(), machine.getFinMaintenance())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La machine " + machine.getCode() + " est en maintenance du " + machine.getDebutMaintenance()
                            + " au " + machine.getFinMaintenance());
        }
        for (OrdreFabricationEntity autre : ordreFabricationRepository.findByMachineIdAndStatutNot(machine.getId(), StatutOrdre.ANNULE)) {
            if (!autre.getId().equals(ordre.getId()) && autre.chevauche(ordre.getDatePrevue(), ordre.getDateFinPrevue())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La machine " + machine.getCode() + " est déjà occupée par l'ordre " + autre.getId()
                                + " du " + autre.getDatePrevue() + " au " + autre.getDateFinPrevue());
            }
        }
    }

    // DELETE /ordres-fabrication/{id} : supprime un ordre PLANIFIE ou ANNULE (404 s'il n'existe pas, 409 sinon)
    public void supprimer(Long id) {
        OrdreFabricationEntity ordre = trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication");
        if (ordre.getStatut() == StatutOrdre.LANCE || ordre.getStatut() == StatutOrdre.TERMINE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un ordre " + ordre.getStatut() + " ne se supprime pas : annulez-le s'il n'est pas terminé");
        }
        ordreFabricationRepository.delete(ordre);
    }

    // F13 - POST /ordres-fabrication/{id}/statut : fait évoluer le statut selon les transitions autorisées (409 sinon) ;
    // F14 - LANCE réserve les composants, TERMINE les consomme, ANNULE après LANCE les libère
    @Transactional
    public OrdreFabricationResponseDTO changerStatut(Long id, StatutOrdre nouveauStatut) {
        OrdreFabricationEntity ordre = trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication");
        if (!ordre.getStatut().statutsAtteignables().contains(nouveauStatut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Passage de " + ordre.getStatut() + " à " + nouveauStatut + " interdit. Statuts atteignables : "
                            + ordre.getStatut().statutsAtteignables());
        }
        if (nouveauStatut == StatutOrdre.LANCE) {
            // F15 - l'ordre reste rattaché à la version de nomenclature en vigueur à son lancement
            ordre.setVersionNomenclature(versionNomenclatureService.versionEnVigueur(ordre.getArticle().getId()));
            reserverComposants(ordre);   // F14
        } else if (nouveauStatut == StatutOrdre.TERMINE) {
            consommerComposants(ordre);
        } else if (ordre.getStatut() == StatutOrdre.LANCE) {
            libererComposants(ordre);    // ANNULE après lancement
        }
        ordre.setStatut(nouveauStatut);   // F13 - la date de modification est horodatée à l'enregistrement
        return entityVersResponseDTO(ordreFabricationRepository.saveAndFlush(ordre));
    }

    // F18 - GET /ordres-fabrication/{id}/besoins : déploiement complet × quantité, comparé au disponible ; un ordre lancé
    // ou terminé utilise sa version figée (pour un ordre TERMINE : les quantités réellement consommées)
    public BesoinsOrdreDTO besoins(Long id) {
        OrdreFabricationEntity ordre = trouverOu404(ordreFabricationRepository, id, "Ordre de fabrication");
        BesoinsOrdreDTO dto = new BesoinsOrdreDTO();
        dto.setOrdreId(ordre.getId());
        dto.setArticleReference(ordre.getArticle().getReference());
        dto.setQuantite(ordre.getQuantite());
        dto.setStatut(ordre.getStatut());
        dto.setBesoinsReels(ordre.getStatut() == StatutOrdre.TERMINE);
        besoinsDe(ordre).forEach((composant, necessaire) -> {
            // un ordre lancé a déjà réservé sa part : elle lui reste disponible
            BigDecimal disponible = composant.quantiteDisponible()
                    .add(ordre.getStatut() == StatutOrdre.LANCE ? necessaire : BigDecimal.ZERO);
            BesoinsOrdreDTO.Ligne ligne = new BesoinsOrdreDTO.Ligne();
            ligne.setReference(composant.getReference());
            ligne.setLibelle(composant.getLibelle());
            ligne.setQuantiteNecessaire(necessaire.setScale(3, RoundingMode.HALF_UP));
            ligne.setQuantiteDisponible(disponible.setScale(3, RoundingMode.HALF_UP));
            ligne.setEcart(disponible.subtract(necessaire).setScale(3, RoundingMode.HALF_UP));
            dto.getBesoins().add(ligne);
        });
        return dto;
    }

    // F14 / F18 - composants achetés nécessaires à l'ordre : version figée de l'article s'il a été lancé ; pour les
    // sous-ensembles, versions en vigueur le jour de sa fin s'il est terminé (ce qui a réellement été consommé), sinon aujourd'hui
    private Map<ArticleEntity, BigDecimal> besoinsDe(OrdreFabricationEntity ordre) {
        LocalDate dateSousEnsembles = (ordre.getStatut() == StatutOrdre.TERMINE)
                ? ordre.getDateModification().toLocalDate()
                : LocalDate.now();
        return deploiementService.besoinsAchetables(ordre.getArticle(), ordre.getQuantite(),
                ordre.getVersionNomenclature(), dateSousEnsembles);
    }

    // F14 - au lancement : chaque composant acheté doit être disponible en quantité suffisante (409 listant les manques),
    // puis les quantités nécessaires sont réservées
    private void reserverComposants(OrdreFabricationEntity ordre) {
        Map<ArticleEntity, BigDecimal> besoins = besoinsDe(ordre);
        List<String> manquants = new ArrayList<>();
        besoins.forEach((composant, necessaire) -> {
            if (composant.quantiteDisponible().compareTo(necessaire) < 0) {
                manquants.add(composant.getReference() + " (manque "
                        + necessaire.subtract(composant.quantiteDisponible()).stripTrailingZeros().toPlainString() + ")");
            }
        });
        if (!manquants.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Lancement impossible, composants manquants : " + String.join(", ", manquants));
        }
        besoins.forEach((composant, necessaire) -> composant.setQuantiteReservee(composant.getQuantiteReservee().add(necessaire)));
    }

    // F14 - à la fin : les composants réservés sortent du stock, et la quantité produite entre en stock
    private void consommerComposants(OrdreFabricationEntity ordre) {
        besoinsDe(ordre).forEach((composant, necessaire) -> {
            composant.setQuantiteStock(composant.getQuantiteStock().subtract(necessaire).max(BigDecimal.ZERO));
            composant.setQuantiteReservee(composant.getQuantiteReservee().subtract(necessaire).max(BigDecimal.ZERO));
        });
        ArticleEntity produit = ordre.getArticle();
        produit.setQuantiteStock(produit.getQuantiteStock().add(ordre.getQuantite()));
    }

    // F14 - un ordre lancé puis annulé rend ses réservations
    private void libererComposants(OrdreFabricationEntity ordre) {
        besoinsDe(ordre).forEach((composant, necessaire) ->
                composant.setQuantiteReservee(composant.getQuantiteReservee().subtract(necessaire).max(BigDecimal.ZERO)));
    }

    // DTO -> Entity (ids transformés en objets, 404 s'ils n'existent pas ; seul un article FABRIQUE avec une durée unitaire
    // se fabrique, 400) ; F12 - fin du créneau = date prévue + quantité × durée unitaire
    private void dtoVersEntity(OrdreFabricationRequestDTO dto, OrdreFabricationEntity ordre) {
        ArticleEntity article = trouverOu404(articleRepository, dto.getArticleId(), "Article");
        MachineEntity machine = trouverOu404(machineRepository, dto.getMachineId(), "Machine");
        OperateurEntity operateur = trouverOu404(operateurRepository, dto.getOperateurId(), "Opérateur");
        if (article.getType() != TypeArticle.FABRIQUE || article.getDureeUnitaireMinutes() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'article " + article.getReference() + " doit être FABRIQUE et avoir une durée unitaire pour être fabriqué");
        }
        long minutes = dto.getQuantite().multiply(BigDecimal.valueOf(article.getDureeUnitaireMinutes()))
                .setScale(0, RoundingMode.CEILING).longValue();

        ordre.setArticle(article);
        ordre.setQuantite(dto.getQuantite());
        ordre.setDatePrevue(dto.getDatePrevue());
        ordre.setDateFinPrevue(dto.getDatePrevue().plusMinutes(minutes));
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
        dto.setDateFinPrevue(ordre.getDateFinPrevue());          // F12
        dto.setStatut(ordre.getStatut());
        dto.setMachineId(ordre.getMachine().getId());
        dto.setMachineCode(ordre.getMachine().getCode());
        dto.setOperateurId(ordre.getOperateur().getId());
        dto.setOperateurNom(ordre.getOperateur().getNom());
        dto.setVersionNomenclature(ordre.getVersionNomenclature());   // F15
        dto.setRetardJours(ordre.getRetardJours());              // F16
        dto.setDateModification(ordre.getDateModification());    // F13
        return dto;
    }
}

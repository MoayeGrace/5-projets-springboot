package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.LigneNomenclatureRequestDTO;
import com.projet3.atelierdeproduction.DTO.LigneNomenclatureResponseDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Entity.TypeArticle;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.LigneNomenclatureRepository;
import com.projet3.atelierdeproduction.Repository.OrdreFabricationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class LigneNomenclatureService {

    private final LigneNomenclatureRepository ligneNomenclatureRepository;
    private final ArticleRepository articleRepository;
    private final OrdreFabricationRepository ordreFabricationRepository;   // F15
    private final VersionNomenclatureService versionNomenclatureService;   // F15

    // GET /nomenclatures : récupère toutes les lignes de nomenclature (toutes versions)
    public List<LigneNomenclatureResponseDTO> lister() {
        return ligneNomenclatureRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /nomenclatures/{id} : récupère une ligne par son id (404 si elle n'existe pas)
    public LigneNomenclatureResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(ligneNomenclatureRepository, id, "Ligne de nomenclature"));
    }

    // F6 - GET /articles/{id}/nomenclature : composants directs de la version en vigueur, triés par référence
    // (vide pour un article ACHETE, 404 si inconnu)
    public List<LigneNomenclatureResponseDTO> composantsDirects(Long articleId) {
        trouverOu404(articleRepository, articleId, "Article");
        return versionNomenclatureService.lignesEnVigueur(articleId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F7 - GET /articles/{id}/utilisations : articles dont la nomenclature en vigueur contient ce composant,
    // avec la quantité par unité produite
    public List<LigneNomenclatureResponseDTO> utilisations(Long articleId) {
        ArticleEntity composant = trouverOu404(articleRepository, articleId, "Article");
        return ligneNomenclatureRepository.findByComposantReferenceOrderByArticleComposeReferenceAsc(composant.getReference())
                .stream()
                .filter(ligne -> ligne.getVersion().equals(
                        versionNomenclatureService.versionEnVigueur(ligne.getArticleCompose().getId())))
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /nomenclatures : ajoute une ligne (INSERT) à la version en préparation s'il y en a une, sinon à la version en vigueur
    // (version 1, appliquée aussitôt, pour une première nomenclature)
    public LigneNomenclatureResponseDTO creer(LigneNomenclatureRequestDTO dto) {
        ArticleEntity compose = trouverOu404(articleRepository, dto.getComposeId(), "Article");
        LigneNomenclatureEntity ligne = new LigneNomenclatureEntity();
        ligne.setArticleCompose(compose);

        Integer enPreparation = ligneNomenclatureRepository.versionEnPreparation(compose.getId());
        Integer enVigueur = versionNomenclatureService.versionEnVigueur(compose.getId());
        if (enPreparation != null) {
            ligne.setVersion(enPreparation);
            ligne.setDateApplication(null);
        } else if (enVigueur != null) {
            ligne.setVersion(enVigueur);
            ligne.setDateApplication(versionNomenclatureService.lignesDeLaVersion(compose.getId(), enVigueur).get(0).getDateApplication());
        } else {
            ligne.setVersion(1);
            ligne.setDateApplication(LocalDate.now());
        }
        verifierVersionModifiable(ligne);   // F15
        dtoVersEntity(dto, ligne);
        return entityVersResponseDTO(ligneNomenclatureRepository.save(ligne));
    }

    // PUT /nomenclatures/{id} : change le composant ou la quantité d'une ligne (404 sinon) ; l'article composé ne change pas (400)
    public LigneNomenclatureResponseDTO modifier(Long id, LigneNomenclatureRequestDTO dto) {
        LigneNomenclatureEntity ligne = trouverOu404(ligneNomenclatureRepository, id, "Ligne de nomenclature");
        if (!ligne.getArticleCompose().getId().equals(dto.getComposeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Une ligne ne change pas d'article composé : supprimez-la puis recréez-la");
        }
        verifierVersionModifiable(ligne);   // F15
        dtoVersEntity(dto, ligne);
        return entityVersResponseDTO(ligneNomenclatureRepository.save(ligne));
    }

    // DELETE /nomenclatures/{id} : supprime la ligne (404 si elle n'existe pas, 409 si sa version est figée)
    public void supprimer(Long id) {
        LigneNomenclatureEntity ligne = trouverOu404(ligneNomenclatureRepository, id, "Ligne de nomenclature");
        verifierVersionModifiable(ligne);   // F15
        ligneNomenclatureRepository.delete(ligne);
    }

    // F15 - une version en préparation se modifie librement ; la version en vigueur seulement si aucun ordre lancé ne l'utilise ;
    // une ancienne version est figée (409, en indiquant d'ouvrir une nouvelle version)
    private void verifierVersionModifiable(LigneNomenclatureEntity ligne) {
        if (ligne.getDateApplication() == null) {
            return;
        }
        Long articleId = ligne.getArticleCompose().getId();
        Integer enVigueur = versionNomenclatureService.versionEnVigueur(articleId);
        boolean ancienne = enVigueur != null && !ligne.getVersion().equals(enVigueur);
        if (ancienne || ordreFabricationRepository.existsByArticleIdAndVersionNomenclature(articleId, ligne.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La version " + ligne.getVersion() + " de " + ligne.getArticleCompose().getReference()
                            + " est figée (utilisée par un ordre lancé ou remplacée) : ouvrez une nouvelle version avec POST /articles/"
                            + articleId + "/nomenclature/version");
        }
    }

    // DTO -> Entity (ids transformés en articles, 404 s'ils n'existent pas ; seul un article FABRIQUE a une nomenclature ;
    // un composant n'apparaît qu'une fois par version, 409 ; F11 - pas de cycle)
    private void dtoVersEntity(LigneNomenclatureRequestDTO dto, LigneNomenclatureEntity ligne) {
        ArticleEntity compose = ligne.getArticleCompose();
        ArticleEntity composant = trouverOu404(articleRepository, dto.getComposantId(), "Article");
        if (compose.getType() != TypeArticle.FABRIQUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'article " + compose.getReference() + " est acheté : il n'a pas de nomenclature");
        }
        boolean doublon = versionNomenclatureService.lignesDeLaVersion(compose.getId(), ligne.getVersion()).stream()
                .anyMatch(autre -> !Objects.equals(autre.getId(), ligne.getId())
                        && autre.getComposantReference().equals(composant.getReference()));
        if (doublon) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    composant.getReference() + " figure déjà dans la version " + ligne.getVersion() + " de " + compose.getReference());
        }
        verifierAbsenceDeCycle(compose, composant);   // F11

        ligne.setComposantReference(composant.getReference());
        ligne.setQuantite(dto.getQuantite());
    }

    // F11 - refuse la ligne si le composant contient déjà, directement ou indirectement, l'article composé (409 avec le cycle)
    private void verifierAbsenceDeCycle(ArticleEntity compose, ArticleEntity composant) {
        List<ArticleEntity> chemin = chercherChemin(composant, compose, new HashSet<>());
        if (chemin != null) {
            String cycle = compose.getReference() + " → " + chemin.stream()
                    .map(ArticleEntity::getReference)
                    .collect(Collectors.joining(" → "));
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cycle détecté dans la nomenclature : " + cycle);
        }
    }

    // F11 - parcours en profondeur (toutes versions confondues) : chemin de "depuis" jusqu'à "cible" en descendant les composants,
    // ou null. "visites" évite de repasser par un article déjà exploré : le parcours termine quelle que soit la profondeur
    private List<ArticleEntity> chercherChemin(ArticleEntity depuis, ArticleEntity cible, Set<Long> visites) {
        if (depuis.getId().equals(cible.getId())) {
            return new ArrayList<>(List.of(depuis));
        }
        if (!visites.add(depuis.getId())) {
            return null;
        }
        for (LigneNomenclatureEntity ligne : depuis.getLignesNomenclature()) {
            ArticleEntity suivant = articleRepository.findByReference(ligne.getComposantReference()).orElse(null);
            List<ArticleEntity> suite = (suivant == null) ? null : chercherChemin(suivant, cible, visites);
            if (suite != null) {
                suite.add(0, depuis);
                return suite;
            }
        }
        return null;
    }

    // Entity -> DTO (le composant est retrouvé par sa référence)
    private LigneNomenclatureResponseDTO entityVersResponseDTO(LigneNomenclatureEntity ligne) {
        LigneNomenclatureResponseDTO dto = new LigneNomenclatureResponseDTO();
        dto.setId(ligne.getId());
        dto.setComposeId(ligne.getArticleCompose().getId());
        dto.setComposeReference(ligne.getArticleCompose().getReference());
        dto.setComposantReference(ligne.getComposantReference());
        articleRepository.findByReference(ligne.getComposantReference()).ifPresent(composant -> {
            dto.setComposantId(composant.getId());
            dto.setComposantLibelle(composant.getLibelle());
            dto.setComposantType(composant.getType());
        });
        dto.setQuantite(ligne.getQuantite());
        dto.setVersion(ligne.getVersion());                    // F15
        dto.setDateApplication(ligne.getDateApplication());
        return dto;
    }
}

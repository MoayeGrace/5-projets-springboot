package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.VersionNomenclatureDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Entity.OrdreFabricationEntity;
import com.projet3.atelierdeproduction.Entity.StatutOrdre;
import com.projet3.atelierdeproduction.Entity.TypeArticle;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.LigneNomenclatureRepository;
import com.projet3.atelierdeproduction.Repository.OrdreFabricationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

// F15 - versions de nomenclature : chaque ligne porte son numéro de version et sa date d'application (null = en préparation)
@Service
@RequiredArgsConstructor
public class VersionNomenclatureService {

    private final LigneNomenclatureRepository ligneRepository;
    private final ArticleRepository articleRepository;
    private final OrdreFabricationRepository ordreRepository;   // F15

    // F15 - numéro de la version en vigueur aujourd'hui (null si l'article n'a pas de nomenclature appliquée)
    public Integer versionEnVigueur(Long articleId) {
        return ligneRepository.versionApplicable(articleId, LocalDate.now());
    }

    // F15 - lignes de la version en vigueur aujourd'hui (vide si aucune)
    public List<LigneNomenclatureEntity> lignesEnVigueur(Long articleId) {
        return lignesDeLaVersion(articleId, versionEnVigueur(articleId));
    }

    // F15 - lignes de la version applicable à une date (vide si aucune)
    public List<LigneNomenclatureEntity> lignesApplicables(Long articleId, LocalDate date) {
        return lignesDeLaVersion(articleId, ligneRepository.versionApplicable(articleId, date));
    }

    // F15 - lignes d'une version donnée, triées par référence du composant (vide si la version est null)
    public List<LigneNomenclatureEntity> lignesDeLaVersion(Long articleId, Integer version) {
        if (version == null) {
            return List.of();
        }
        return ligneRepository.findByArticleComposeIdAndVersionOrderByComposantReferenceAsc(articleId, version);
    }

    // F15 - POST /articles/{id}/nomenclature/version : recopie la version en vigueur dans une nouvelle version en préparation,
    // que l'on modifie ensuite avec /nomenclatures (400 pour un article acheté, 409 si une version est déjà en préparation
    // ou s'il n'y a rien à versionner)
    @Transactional
    public VersionNomenclatureDTO ouvrirVersion(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        if (article.getType() != TypeArticle.FABRIQUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'article " + article.getReference() + " est acheté : il n'a pas de nomenclature");
        }
        Integer enPreparation = ligneRepository.versionEnPreparation(articleId);
        if (enPreparation != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La version " + enPreparation + " de " + article.getReference() + " est déjà en préparation");
        }
        List<LigneNomenclatureEntity> actuelles = lignesEnVigueur(articleId);
        if (actuelles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    article.getReference() + " n'a pas de nomenclature en vigueur : saisissez ses lignes avec POST /nomenclatures");
        }
        int numero = ligneRepository.derniereVersion(articleId) + 1;
        for (LigneNomenclatureEntity ligne : actuelles) {
            LigneNomenclatureEntity copie = new LigneNomenclatureEntity();
            copie.setArticleCompose(article);
            copie.setComposantReference(ligne.getComposantReference());
            copie.setQuantite(ligne.getQuantite());
            copie.setVersion(numero);
            copie.setDateApplication(null);   // en préparation
            ligneRepository.save(copie);
        }
        return versionVersDTO(article, lignesDeLaVersion(articleId, numero));
    }

    // F15 - POST /articles/{id}/nomenclature/version/application : date la version en préparation d'aujourd'hui ;
    // elle devient la nomenclature en vigueur (409 si aucune version n'est en préparation)
    @Transactional
    public VersionNomenclatureDTO appliquerVersion(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        Integer numero = ligneRepository.versionEnPreparation(articleId);
        if (numero == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucune version en préparation pour " + article.getReference());
        }
        verifierAucunOrdreDependant(article);
        List<LigneNomenclatureEntity> lignes = lignesDeLaVersion(articleId, numero);
        lignes.forEach(ligne -> ligne.setDateApplication(LocalDate.now()));
        return versionVersDTO(article, lignes);
    }

    // F15 - un ordre lancé (ou terminé aujourd'hui) qui utilise l'article comme sous-ensemble doit garder la version qu'il a
    // réservée ou consommée : la nouvelle version ne s'applique pas tant qu'il existe (409 citant l'ordre)
    private void verifierAucunOrdreDependant(ArticleEntity article) {
        List<OrdreFabricationEntity> ordres = new ArrayList<>(ordreRepository.findByStatutOrderByDatePrevueAsc(StatutOrdre.LANCE));
        ordres.addAll(ordreRepository.findByStatutAndDateModificationGreaterThanEqual(StatutOrdre.TERMINE, LocalDate.now().atStartOfDay()));
        for (OrdreFabricationEntity ordre : ordres) {
            List<LigneNomenclatureEntity> lignes = lignesDeLaVersion(ordre.getArticle().getId(), ordre.getVersionNomenclature());
            if (contientSousEnsemble(lignes, article.getId(), new HashSet<>())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La nouvelle version de " + article.getReference() + " ne peut pas s'appliquer : l'ordre " + ordre.getId()
                                + " (" + ordre.getStatut() + ", " + ordre.getArticle().getReference() + ") l'utilise comme sous-ensemble ; "
                                + (ordre.getStatut() == StatutOrdre.LANCE
                                    ? "terminez-le ou annulez-le d'abord"
                                    : "il a été terminé aujourd'hui avec la version actuelle : la nouvelle pourra s'appliquer demain"));
            }
        }
    }

    // F15 - vrai si l'article cherché est un composant fabriqué des lignes, à n'importe quel niveau
    private boolean contientSousEnsemble(List<LigneNomenclatureEntity> lignes, Long articleId, Set<Long> vus) {
        for (LigneNomenclatureEntity ligne : lignes) {
            ArticleEntity composant = articleRepository.findByReference(ligne.getComposantReference()).orElse(null);
            if (composant == null || composant.getType() != TypeArticle.FABRIQUE || !vus.add(composant.getId())) {
                continue;
            }
            if (composant.getId().equals(articleId) || contientSousEnsemble(lignesEnVigueur(composant.getId()), articleId, vus)) {
                return true;
            }
        }
        return false;
    }

    // F15 - GET /articles/{id}/nomenclature/versions : toutes les versions de l'article, avec leurs lignes
    public List<VersionNomenclatureDTO> lister(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        Map<Integer, List<LigneNomenclatureEntity>> parVersion = new LinkedHashMap<>();
        for (LigneNomenclatureEntity ligne : ligneRepository.findByArticleComposeIdOrderByVersionAscComposantReferenceAsc(articleId)) {
            parVersion.computeIfAbsent(ligne.getVersion(), v -> new ArrayList<>()).add(ligne);
        }
        return parVersion.values().stream().map(lignes -> versionVersDTO(article, lignes)).toList();
    }

    // F15 - GET /articles/{id}/nomenclature/versions/en-vigueur?date= : version applicable à une date (404 si aucune)
    public VersionNomenclatureDTO versionALaDate(Long articleId, LocalDate date) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        Integer numero = ligneRepository.versionApplicable(articleId, date);
        if (numero == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune version de nomenclature n'était appliquée le " + date);
        }
        return versionVersDTO(article, lignesDeLaVersion(articleId, numero));
    }

    // F15 - lignes d'une même version -> DTO
    private VersionNomenclatureDTO versionVersDTO(ArticleEntity article, List<LigneNomenclatureEntity> lignes) {
        VersionNomenclatureDTO dto = new VersionNomenclatureDTO();
        dto.setArticleId(article.getId());
        dto.setArticleReference(article.getReference());
        dto.setNumero(lignes.get(0).getVersion());
        dto.setDateApplication(lignes.get(0).getDateApplication());
        dto.setEnPreparation(lignes.get(0).getDateApplication() == null);
        for (LigneNomenclatureEntity ligne : lignes) {
            VersionNomenclatureDTO.Ligne l = new VersionNomenclatureDTO.Ligne();
            l.setComposantReference(ligne.getComposantReference());
            l.setQuantite(ligne.getQuantite());
            dto.getLignes().add(l);
        }
        return dto;
    }
}

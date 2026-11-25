package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.VersionNomenclatureDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.LigneNomenclatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

// F15 - versions de nomenclature : chaque ligne porte son numéro de version et sa date d'application (null = en préparation)
@Service
@RequiredArgsConstructor
public class VersionNomenclatureService {

    private final LigneNomenclatureRepository ligneRepository;
    private final ArticleRepository articleRepository;

    // F15 - numéro de la version en vigueur aujourd'hui (null si l'article n'a pas de nomenclature appliquée)
    public Integer versionEnVigueur(Long articleId) {
        return ligneRepository.versionApplicable(articleId, LocalDate.now());
    }

    // F15 - lignes de la version en vigueur aujourd'hui (vide si aucune)
    public List<LigneNomenclatureEntity> lignesEnVigueur(Long articleId) {
        return lignesDeLaVersion(articleId, versionEnVigueur(articleId));
    }

    // F15 - lignes d'une version donnée, triées par référence du composant (vide si la version est null)
    public List<LigneNomenclatureEntity> lignesDeLaVersion(Long articleId, Integer version) {
        if (version == null) {
            return List.of();
        }
        return ligneRepository.findByArticleComposeIdAndVersionOrderByComposantReferenceAsc(articleId, version);
    }

    // F15 - POST /articles/{id}/nomenclature/version : recopie la version en vigueur dans une nouvelle version en préparation,
    // que l'on modifie ensuite avec /nomenclatures (409 si une version est déjà en préparation ou s'il n'y a rien à versionner)
    @Transactional
    public VersionNomenclatureDTO ouvrirVersion(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
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
        List<LigneNomenclatureEntity> lignes = lignesDeLaVersion(articleId, numero);
        lignes.forEach(ligne -> ligne.setDateApplication(LocalDate.now()));
        return versionVersDTO(article, lignes);
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

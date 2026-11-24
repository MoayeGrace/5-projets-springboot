package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.VersionNomenclatureDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Entity.LigneVersionEntity;
import com.projet3.atelierdeproduction.Entity.StatutVersion;
import com.projet3.atelierdeproduction.Entity.VersionNomenclatureEntity;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import com.projet3.atelierdeproduction.Repository.VersionNomenclatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class VersionNomenclatureService {

    private final VersionNomenclatureRepository versionRepository;
    private final ArticleRepository articleRepository;

    // F15 - POST /articles/{id}/nomenclature/version : ouvre une nouvelle version en préparation (409 s'il y en a déjà une)
    public VersionNomenclatureDTO ouvrirVersion(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        versionRepository.findFirstByArticleIdAndStatut(articleId, StatutVersion.EN_PREPARATION).ifPresent(v -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La version " + v.getNumero() + " de " + article.getReference() + " est déjà en préparation");
        });
        VersionNomenclatureEntity version = new VersionNomenclatureEntity();
        version.setArticle(article);
        version.setNumero(versionRepository.findByArticleIdOrderByNumeroAsc(articleId).size() + 1);
        return entityVersDTO(versionRepository.save(version));
    }

    // F15 - POST /articles/{id}/nomenclature/version/application : fige les lignes actuelles dans la version en préparation
    // et la date ; elle devient la nomenclature en vigueur à partir de maintenant (409 si aucune version n'est en préparation)
    @Transactional
    public VersionNomenclatureDTO appliquerVersion(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        VersionNomenclatureEntity version = versionRepository
                .findFirstByArticleIdAndStatut(articleId, StatutVersion.EN_PREPARATION)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Aucune version en préparation pour " + article.getReference()));

        for (LigneNomenclatureEntity ligne : article.getComposants()) {
            LigneVersionEntity copie = new LigneVersionEntity();
            copie.setVersion(version);
            copie.setComposant(ligne.getComposant());
            copie.setQuantite(ligne.getQuantite());
            version.getLignes().add(copie);
        }
        version.setStatut(StatutVersion.APPLIQUEE);
        version.setDateApplication(LocalDateTime.now());
        return entityVersDTO(versionRepository.save(version));
    }

    // F15 - GET /articles/{id}/nomenclature/versions : toutes les versions de l'article
    public List<VersionNomenclatureDTO> lister(Long articleId) {
        trouverOu404(articleRepository, articleId, "Article");
        return versionRepository.findByArticleIdOrderByNumeroAsc(articleId).stream().map(this::entityVersDTO).toList();
    }

    // F15 - GET /articles/{id}/nomenclature/versions/en-vigueur?date= : version applicable à une date passée (404 si aucune)
    public VersionNomenclatureDTO versionALaDate(Long articleId, LocalDate date) {
        trouverOu404(articleRepository, articleId, "Article");
        return versionEnVigueur(articleId, date.plusDays(1).atStartOfDay().minusNanos(1))
                .map(this::entityVersDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune version de nomenclature n'était appliquée le " + date));
    }

    // F15 - version appliquée en vigueur à un instant donné (utilisée aussi au lancement d'un ordre)
    public Optional<VersionNomenclatureEntity> versionEnVigueur(Long articleId, LocalDateTime instant) {
        return versionRepository.findFirstByArticleIdAndStatutAndDateApplicationLessThanEqualOrderByDateApplicationDesc(
                articleId, StatutVersion.APPLIQUEE, instant);
    }

    // Entity -> DTO
    private VersionNomenclatureDTO entityVersDTO(VersionNomenclatureEntity version) {
        VersionNomenclatureDTO dto = new VersionNomenclatureDTO();
        dto.setId(version.getId());
        dto.setArticleId(version.getArticle().getId());
        dto.setArticleReference(version.getArticle().getReference());
        dto.setNumero(version.getNumero());
        dto.setStatut(version.getStatut());
        dto.setDateCreation(version.getDateCreation());
        dto.setDateApplication(version.getDateApplication());
        for (LigneVersionEntity ligne : version.getLignes()) {
            VersionNomenclatureDTO.Ligne l = new VersionNomenclatureDTO.Ligne();
            l.setComposantReference(ligne.getComposant().getReference());
            l.setQuantite(ligne.getQuantite());
            dto.getLignes().add(l);
        }
        return dto;
    }
}

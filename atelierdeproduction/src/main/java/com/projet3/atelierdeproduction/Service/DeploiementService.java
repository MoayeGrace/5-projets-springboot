package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.NomenclatureCompleteDTO;
import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Entity.TypeArticle;
import com.projet3.atelierdeproduction.Repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

// Déploiement de la nomenclature : descente récursive jusqu'aux articles achetés
@Service
@RequiredArgsConstructor
public class DeploiementService {

    private final ArticleRepository articleRepository;   // F17

    // F14 - quantité totale de chaque composant ACHETE nécessaire pour produire "quantite" unités de l'article
    // (les quantités sont multipliées à chaque niveau ; la nomenclature est un arbre grâce à F11, donc la descente termine)
    public Map<ArticleEntity, BigDecimal> besoinsAchetables(ArticleEntity article, BigDecimal quantite) {
        Map<ArticleEntity, BigDecimal> besoins = new LinkedHashMap<>();
        cumulerBesoins(article, quantite, besoins);
        return besoins;
    }

    // F17 - GET /articles/{id}/nomenclature-complete : arbre complet jusqu'aux articles achetés (quantités cumulées
    // à chaque niveau), profondeur atteinte, et liste à plat des composants achetables pour 1 unité
    public NomenclatureCompleteDTO nomenclatureComplete(Long articleId) {
        ArticleEntity article = trouverOu404(articleRepository, articleId, "Article");
        NomenclatureCompleteDTO dto = new NomenclatureCompleteDTO();
        dto.setArbre(construireNoeud(article, BigDecimal.ONE, 0));
        dto.setProfondeur(profondeur(dto.getArbre()));
        besoinsAchetables(article, BigDecimal.ONE).forEach((composant, quantite) -> {
            NomenclatureCompleteDTO.Besoin besoin = new NomenclatureCompleteDTO.Besoin();
            besoin.setReference(composant.getReference());
            besoin.setLibelle(composant.getLibelle());
            besoin.setQuantiteTotale(quantite.setScale(3, RoundingMode.HALF_UP));
            dto.getAchetables().add(besoin);
        });
        return dto;
    }

    // F17 - construit le nœud d'un article puis, récursivement, ceux de ses composants
    private NomenclatureCompleteDTO.Noeud construireNoeud(ArticleEntity article, BigDecimal quantiteCumulee, int niveau) {
        NomenclatureCompleteDTO.Noeud noeud = new NomenclatureCompleteDTO.Noeud();
        noeud.setReference(article.getReference());
        noeud.setLibelle(article.getLibelle());
        noeud.setType(article.getType());
        noeud.setQuantiteCumulee(quantiteCumulee.setScale(3, RoundingMode.HALF_UP));
        noeud.setNiveau(niveau);
        for (LigneNomenclatureEntity ligne : article.getComposants()) {
            noeud.getComposants().add(
                    construireNoeud(ligne.getComposant(), quantiteCumulee.multiply(ligne.getQuantite()), niveau + 1));
        }
        return noeud;
    }

    // F17 - niveau le plus profond de l'arbre
    private int profondeur(NomenclatureCompleteDTO.Noeud noeud) {
        return noeud.getComposants().stream().mapToInt(this::profondeur).max().orElse(noeud.getNiveau());
    }

    // F14 - ajoute aux besoins les composants achetés de l'article, en descendant dans les composants fabriqués
    private void cumulerBesoins(ArticleEntity article, BigDecimal quantite, Map<ArticleEntity, BigDecimal> besoins) {
        for (LigneNomenclatureEntity ligne : article.getComposants()) {
            ArticleEntity composant = ligne.getComposant();
            BigDecimal quantiteComposant = quantite.multiply(ligne.getQuantite());
            if (composant.getType() == TypeArticle.ACHETE) {
                besoins.merge(composant, quantiteComposant, BigDecimal::add);
            } else {
                cumulerBesoins(composant, quantiteComposant, besoins);
            }
        }
    }
}

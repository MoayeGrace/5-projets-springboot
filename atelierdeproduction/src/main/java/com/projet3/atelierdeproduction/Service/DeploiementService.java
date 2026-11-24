package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.Entity.ArticleEntity;
import com.projet3.atelierdeproduction.Entity.LigneNomenclatureEntity;
import com.projet3.atelierdeproduction.Entity.TypeArticle;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

// Déploiement de la nomenclature : descente récursive jusqu'aux articles achetés
@Service
public class DeploiementService {

    // F14 - quantité totale de chaque composant ACHETE nécessaire pour produire "quantite" unités de l'article
    // (les quantités sont multipliées à chaque niveau ; la nomenclature est un arbre grâce à F11, donc la descente termine)
    public Map<ArticleEntity, BigDecimal> besoinsAchetables(ArticleEntity article, BigDecimal quantite) {
        Map<ArticleEntity, BigDecimal> besoins = new LinkedHashMap<>();
        cumulerBesoins(article, quantite, besoins);
        return besoins;
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

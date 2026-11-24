package com.projet1.placedemarche.DTO;

import lombok.Getter;

import java.math.BigDecimal;

// F18 - un produit du classement : quantité commandée, chiffre d'affaires et vendeur
@Getter
public class TopProduitDTO {

    private final Long produitId;
    private final String produitNom;
    private final String vendeurRaisonSociale;
    private final Long quantite;
    private final BigDecimal chiffreAffaires;

    // F18 - constructeur appelé directement par la requête JPQL "SELECT new ...TopProduitDTO(...)"
    public TopProduitDTO(Long produitId, String produitNom, String vendeurRaisonSociale,
                         Long quantite, BigDecimal chiffreAffaires) {
        this.produitId = produitId;
        this.produitNom = produitNom;
        this.vendeurRaisonSociale = vendeurRaisonSociale;
        this.quantite = quantite;
        this.chiffreAffaires = chiffreAffaires;
    }
}

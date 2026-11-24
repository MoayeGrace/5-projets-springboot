package com.projet1.placedemarche.DTO;

import com.projet1.placedemarche.Entity.StatutCommande;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommandeResponseDTO {

    private Long id;

    private Long clientId;
    private String clientNom;

    private Long produitId;
    private String produitNom;

    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montant;

    private StatutCommande statut;
    private LocalDateTime dateCommande;
    private LocalDateTime dateModification;
    private LocalDateTime dateLivraison;   // F17 - remplie au passage à LIVREE
}

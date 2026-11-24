package com.projet1.placedemarche.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommandeRequestDTO {

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être supérieure à 0")
    private Integer quantite;

    // F13 - plus de statut ici : il évolue uniquement via POST /commandes/{id}/statut (transitions contrôlées)
    // F15 - pas de prixUnitaire ici : il est copié depuis le produit
}

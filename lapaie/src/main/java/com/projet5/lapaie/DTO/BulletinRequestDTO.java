package com.projet5.lapaie.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BulletinRequestDTO {

    @NotNull(message = "L'employé est obligatoire")
    private Long employeId;

    @NotNull(message = "Le mois est obligatoire")
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "Le mois doit être au format AAAA-MM")
    private String mois;

    // F13 - à la création, brut et net sont calculés (ces champs sont ignorés) ; en modification, ils corrigent un montant
    @PositiveOrZero(message = "Le brut ne peut pas être négatif")
    private BigDecimal brut;

    private BigDecimal net;
}

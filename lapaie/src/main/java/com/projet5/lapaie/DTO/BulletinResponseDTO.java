package com.projet5.lapaie.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BulletinResponseDTO {

    private Long id;

    private Long employeId;
    private String employeNom;

    private String mois;
    private BigDecimal brut;
    private BigDecimal net;
    private boolean edite;
}

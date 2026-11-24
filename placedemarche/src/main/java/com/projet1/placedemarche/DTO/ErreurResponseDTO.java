package com.projet1.placedemarche.DTO;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)   // n'affiche pas "erreurs" quand il est null
public class ErreurResponseDTO {

    private int status;                       // code HTTP (400, 404, 409...)
    private String message;                   // message général
    private Map<String, String> erreurs;      // détail par champ (seulement pour la validation)

    // Constructeur utilisé par GestionnaireErreurs : new ErreurResponseDTO(404, "Vendeur 99 introuvable", null)
    public ErreurResponseDTO(int status, String message, Map<String, String> erreurs) {
        this.status = status;
        this.message = message;
        this.erreurs = erreurs;
    }
}

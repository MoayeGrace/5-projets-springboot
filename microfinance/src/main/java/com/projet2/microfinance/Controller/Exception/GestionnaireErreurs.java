package com.projet2.microfinance.Controller.Exception;

import com.projet2.microfinance.DTO.ErreurResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GestionnaireErreurs {

    // 400 : un champ du DTO ne respecte pas ses annotations (@NotBlank, @Positive...), déclenché par @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponseDTO> validation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        for (FieldError erreur : ex.getBindingResult().getFieldErrors()) {
            erreurs.put(erreur.getField(), erreur.getDefaultMessage());   // ex : "montant" -> "Le montant doit être positif"
        }
        return reponse(HttpStatus.BAD_REQUEST, "Données invalides", erreurs);
    }

    // 400 : le JSON est mal écrit, ou une valeur n'a pas le bon type (ex : type "LIVRET" au lieu de "EPARGNE")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurResponseDTO> jsonIllisible(HttpMessageNotReadableException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Le JSON envoyé est invalide ou contient une valeur incorrecte", null);
    }

    // 400 : un paramètre de l'URL n'a pas le bon type (ex : /clients/abc au lieu d'un nombre)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurResponseDTO> mauvaisType(MethodArgumentTypeMismatchException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Paramètre invalide : " + ex.getName(), null);
    }

    // 404 (ou autre) : erreurs lancées par nos Services, ex : trouverOu404 -> "Client 99 introuvable"
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErreurResponseDTO> statut(ResponseStatusException ex) {
        return reponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    // 409 : la base refuse l'opération (valeur unique en double, ou élément encore utilisé ailleurs)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurResponseDTO> conflit(DataIntegrityViolationException ex) {
        String cause = String.valueOf(ex.getMostSpecificCause().getMessage());
        String message;
        if (cause.contains("Duplicate entry")) {
            message = "Cette valeur existe déjà (numéro de compte déjà utilisé)";
        } else if (cause.contains("foreign key")) {
            message = "Opération impossible : cet élément est encore utilisé par d'autres données";
        } else {
            message = "Opération refusée par la base de données";
        }
        return reponse(HttpStatus.CONFLICT, message, null);
    }

    // Construit la réponse JSON commune à toutes les erreurs
    private ResponseEntity<ErreurResponseDTO> reponse(HttpStatus status, String message, Map<String, String> erreurs) {
        return ResponseEntity.status(status).body(new ErreurResponseDTO(status.value(), message, erreurs));
    }
}

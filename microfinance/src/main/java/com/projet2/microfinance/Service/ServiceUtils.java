package com.projet2.microfinance.Service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ServiceUtils {

    private ServiceUtils() {
    }

    // Cherche un élément par son id dans n'importe quel Repository, sinon erreur 404
    public static <T> T trouverOu404(JpaRepository<T, Long> repository, Long id, String nom) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, nom + " " + id + " introuvable"));
    }
}

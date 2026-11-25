package com.projet5.lapaie.DTO;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

// F10 - réponse paginée : la page demandée + le nombre total d'éléments et de pages
@Getter
public class PageResponseDTO<T> {

    private final List<T> contenu;        // les éléments de la page
    private final int page;               // numéro de la page (commence à 0)
    private final int taille;             // nombre d'éléments par page
    private final long totalElements;     // nombre total d'éléments, toutes pages confondues
    private final int totalPages;         // nombre total de pages

    // F10 - construit la réponse à partir d'une Page Spring Data dont le contenu est déjà converti en DTO
    public PageResponseDTO(Page<T> page) {
        this.contenu = page.getContent();
        this.page = page.getNumber();
        this.taille = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}

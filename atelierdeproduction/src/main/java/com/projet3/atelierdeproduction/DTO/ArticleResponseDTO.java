package com.projet3.atelierdeproduction.DTO;

import com.projet3.atelierdeproduction.Entity.TypeArticle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArticleResponseDTO {

    private Long id;
    private String reference;
    private String libelle;
    private String unite;
    private TypeArticle type;
}

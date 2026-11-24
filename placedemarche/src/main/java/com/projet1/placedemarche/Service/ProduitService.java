package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.PageResponseDTO;
import com.projet1.placedemarche.DTO.ProduitRequestDTO;
import com.projet1.placedemarche.DTO.ProduitResponseDTO;
import com.projet1.placedemarche.Entity.CategorieEntity;
import com.projet1.placedemarche.Entity.ProduitEntity;
import com.projet1.placedemarche.Entity.VendeurEntity;
import com.projet1.placedemarche.Repository.CategorieRepository;
import com.projet1.placedemarche.Repository.ProduitRepository;
import com.projet1.placedemarche.Repository.VendeurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final VendeurRepository vendeurRepository;
    private final CategorieRepository categorieRepository;

    // F10 - taille de page maximale autorisée
    private static final int TAILLE_PAGE_MAX = 100;

    // F9 + F10 - GET /produits?nom=&prixMin=&prixMax=&page=&size=&sort= : recherche par critères facultatifs, page par page
    public PageResponseDTO<ProduitResponseDTO> rechercher(String nom, BigDecimal prixMin, BigDecimal prixMax, Pageable pageable) {
        if (prixMin != null && prixMax != null && prixMin.compareTo(prixMax) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le prix minimum ne peut pas être supérieur au prix maximum");
        }
        String nomCherche = (nom == null || nom.isBlank()) ? null : nom.trim();

        // F10 - "une taille supérieure à 100 est ramenée à 100" (on garde le numéro de page et le tri demandés)
        if (pageable.getPageSize() > TAILLE_PAGE_MAX) {
            pageable = PageRequest.of(pageable.getPageNumber(), TAILLE_PAGE_MAX, pageable.getSort());
        }

        // F10 - map() convertit chaque entité de la page en DTO, en conservant les infos de pagination
        return new PageResponseDTO<>(produitRepository.rechercher(nomCherche, prixMin, prixMax, pageable)
                .map(this::entityVersResponseDTO));
    }

    // GET /produits/{id} : récupère un produit par son id (404 s'il n'existe pas)
    public ProduitResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(produitRepository, id, "Produit"));
    }

    // F6 - GET /vendeurs/{id}/produits : produits d'un vendeur triés par nom (404 si le vendeur n'existe pas)
    public List<ProduitResponseDTO> listerParVendeur(Long vendeurId) {
        trouverOu404(vendeurRepository, vendeurId, "Vendeur");
        return produitRepository.findByVendeurIdOrderByNomAsc(vendeurId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F7 - GET /categories/{id}/produits : produits d'une catégorie triés par prix croissant (404 si la catégorie n'existe pas)
    public List<ProduitResponseDTO> listerParCategorie(Long categorieId) {
        trouverOu404(categorieRepository, categorieId, "Catégorie");
        return produitRepository.findByCategorieIdOrderByPrixAsc(categorieId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /produits : crée un nouveau produit (INSERT), rattaché à son vendeur et à sa catégorie
    public ProduitResponseDTO creer(ProduitRequestDTO dto) {
        ProduitEntity produit = new ProduitEntity();
        dtoVersEntity(dto, produit);
        return entityVersResponseDTO(produitRepository.save(produit));
    }

    // PUT /produits/{id} : charge le produit existant (404 sinon), remplace ses champs, puis UPDATE
    public ProduitResponseDTO modifier(Long id, ProduitRequestDTO dto) {
        ProduitEntity produit = trouverOu404(produitRepository, id, "Produit");
        dtoVersEntity(dto, produit);
        return entityVersResponseDTO(produitRepository.save(produit));
    }

    // DELETE /produits/{id} : vérifie que le produit existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        produitRepository.delete(trouverOu404(produitRepository, id, "Produit"));
    }

    // DTO -> Entity (vendeurId et categorieId sont transformés en objets, 404 s'ils n'existent pas)
    private void dtoVersEntity(ProduitRequestDTO dto, ProduitEntity produit) {
        VendeurEntity vendeur = trouverOu404(vendeurRepository, dto.getVendeurId(), "Vendeur");
        CategorieEntity categorie = trouverOu404(categorieRepository, dto.getCategorieId(), "Catégorie");

        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrix(dto.getPrix());
        produit.setStock(dto.getStock());
        produit.setVendeur(vendeur);
        produit.setCategorie(categorie);
    }

    // Entity -> DTO
    private ProduitResponseDTO entityVersResponseDTO(ProduitEntity produit) {
        ProduitResponseDTO dto = new ProduitResponseDTO();
        dto.setId(produit.getId());
        dto.setNom(produit.getNom());
        dto.setDescription(produit.getDescription());
        dto.setPrix(produit.getPrix());
        dto.setStock(produit.getStock());
        dto.setEnRupture(produit.getStock() == 0);
        dto.setVendeurId(produit.getVendeur().getId());
        dto.setVendeurRaisonSociale(produit.getVendeur().getRaisonSociale());
        dto.setCategorieId(produit.getCategorie().getId());
        dto.setCategorieLibelle(produit.getCategorie().getLibelle());
        return dto;
    }
}

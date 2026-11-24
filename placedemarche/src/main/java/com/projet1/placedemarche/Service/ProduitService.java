package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.ProduitRequestDTO;
import com.projet1.placedemarche.DTO.ProduitResponseDTO;
import com.projet1.placedemarche.Entity.CategorieEntity;
import com.projet1.placedemarche.Entity.ProduitEntity;
import com.projet1.placedemarche.Entity.VendeurEntity;
import com.projet1.placedemarche.Repository.CategorieRepository;
import com.projet1.placedemarche.Repository.ProduitRepository;
import com.projet1.placedemarche.Repository.VendeurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final VendeurRepository vendeurRepository;
    private final CategorieRepository categorieRepository;

    // GET /produits : récupère tous les produits et les convertit en DTO
    public List<ProduitResponseDTO> lister() {
        return produitRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /produits/{id} : récupère un produit par son id (404 s'il n'existe pas)
    public ProduitResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(produitRepository, id, "Produit"));
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
        dto.setVendeurId(produit.getVendeur().getId());
        dto.setVendeurRaisonSociale(produit.getVendeur().getRaisonSociale());
        dto.setCategorieId(produit.getCategorie().getId());
        dto.setCategorieLibelle(produit.getCategorie().getLibelle());
        return dto;
    }
}

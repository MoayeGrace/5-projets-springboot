package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.VendeurRequestDTO;
import com.projet1.placedemarche.DTO.VendeurResponseDTO;
import com.projet1.placedemarche.Entity.VendeurEntity;
import com.projet1.placedemarche.Repository.VendeurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class VendeurService {

    private final VendeurRepository vendeurRepository;

    // GET /vendeurs : récupère tous les vendeurs et les convertit en DTO
    public List<VendeurResponseDTO> lister() {
        return vendeurRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /vendeurs/{id} : récupère un vendeur par son id (404 s'il n'existe pas)
    public VendeurResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(vendeurRepository, id, "Vendeur"));
    }

    // POST /vendeurs : crée un nouveau vendeur (INSERT) et renvoie le vendeur créé avec son id
    public VendeurResponseDTO creer(VendeurRequestDTO dto) {
        VendeurEntity vendeur = new VendeurEntity();
        dtoVersEntity(dto, vendeur);
        return entityVersResponseDTO(vendeurRepository.save(vendeur));
    }

    // PUT /vendeurs/{id} : charge le vendeur existant (404 sinon), remplace ses champs, puis UPDATE
    public VendeurResponseDTO modifier(Long id, VendeurRequestDTO dto) {
        VendeurEntity vendeur = trouverOu404(vendeurRepository, id, "Vendeur");
        dtoVersEntity(dto, vendeur);
        return entityVersResponseDTO(vendeurRepository.save(vendeur));
    }

    // DELETE /vendeurs/{id} : vérifie que le vendeur existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        vendeurRepository.delete(trouverOu404(vendeurRepository, id, "Vendeur"));
    }

    // DTO -> Entity
    private void dtoVersEntity(VendeurRequestDTO dto, VendeurEntity vendeur) {
        vendeur.setRaisonSociale(dto.getRaisonSociale());
        vendeur.setEmail(dto.getEmail());
        vendeur.setVille(dto.getVille());
    }

    // Entity -> DTO
    private VendeurResponseDTO entityVersResponseDTO(VendeurEntity vendeur) {
        VendeurResponseDTO dto = new VendeurResponseDTO();
        dto.setId(vendeur.getId());
        dto.setRaisonSociale(vendeur.getRaisonSociale());
        dto.setEmail(vendeur.getEmail());
        dto.setVille(vendeur.getVille());
        return dto;
    }
}

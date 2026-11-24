package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.ConducteurRequestDTO;
import com.projet4.transporturbain.DTO.ConducteurResponseDTO;
import com.projet4.transporturbain.Entity.ConducteurEntity;
import com.projet4.transporturbain.Repository.ConducteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ConducteurService {

    private final ConducteurRepository conducteurRepository;

    // GET /conducteurs : récupère tout l'effectif des conducteurs
    public List<ConducteurResponseDTO> lister() {
        return conducteurRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /conducteurs/{id} : récupère un conducteur par son id (404 si introuvable)
    public ConducteurResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(conducteurRepository, id, "Conducteur"));
    }

    // POST /conducteurs : crée une nouvelle fiche conducteur (INSERT)
    public ConducteurResponseDTO creer(ConducteurRequestDTO dto) {
        ConducteurEntity conducteur = new ConducteurEntity();
        dtoVersEntity(dto, conducteur);
        return entityVersResponseDTO(conducteurRepository.save(conducteur));
    }

    // PUT /conducteurs/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public ConducteurResponseDTO modifier(Long id, ConducteurRequestDTO dto) {
        ConducteurEntity conducteur = trouverOu404(conducteurRepository, id, "Conducteur");
        dtoVersEntity(dto, conducteur);
        return entityVersResponseDTO(conducteurRepository.save(conducteur));
    }

    // DELETE /conducteurs/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        conducteurRepository.delete(trouverOu404(conducteurRepository, id, "Conducteur"));
    }

    // DTO -> Entity
    private void dtoVersEntity(ConducteurRequestDTO dto, ConducteurEntity conducteur) {
        conducteur.setNom(dto.getNom());
        conducteur.setMatricule(dto.getMatricule());
        conducteur.setNumeroPermis(dto.getNumeroPermis());
        if (!dto.getDateValiditePermis().equals(conducteur.getDateValiditePermis())) {
            conducteur.setAlertePermisEnvoyee(false);   // F16 - nouveau permis : il pourra de nouveau être signalé
        }
        conducteur.setDateValiditePermis(dto.getDateValiditePermis());   // F14 - un renouvellement rend le conducteur affectable
    }

    // Entity -> DTO
    private ConducteurResponseDTO entityVersResponseDTO(ConducteurEntity conducteur) {
        ConducteurResponseDTO dto = new ConducteurResponseDTO();
        dto.setId(conducteur.getId());
        dto.setNom(conducteur.getNom());
        dto.setMatricule(conducteur.getMatricule());
        dto.setNumeroPermis(conducteur.getNumeroPermis());
        dto.setDateValiditePermis(conducteur.getDateValiditePermis());   // F14
        return dto;
    }
}
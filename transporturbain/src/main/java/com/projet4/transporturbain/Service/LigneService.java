package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.LigneRequestDTO;
import com.projet4.transporturbain.DTO.LigneResponseDTO;
import com.projet4.transporturbain.Entity.LigneEntity;
import com.projet4.transporturbain.Repository.LigneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class LigneService {

    private final LigneRepository ligneRepository;

    // GET /lignes : récupère tout le réseau de lignes
    public List<LigneResponseDTO> lister() {
        return ligneRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /lignes/{id} : récupère une ligne par son id (404 si introuvable)
    public LigneResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(ligneRepository, id, "Ligne"));
    }

    // POST /lignes : crée une nouvelle ligne (INSERT)
    public LigneResponseDTO creer(LigneRequestDTO dto) {
        LigneEntity ligne = new LigneEntity();
        dtoVersEntity(dto, ligne);
        return entityVersResponseDTO(ligneRepository.save(ligne));
    }

    // PUT /lignes/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public LigneResponseDTO modifier(Long id, LigneRequestDTO dto) {
        LigneEntity ligne = trouverOu404(ligneRepository, id, "Ligne");
        dtoVersEntity(dto, ligne);
        return entityVersResponseDTO(ligneRepository.save(ligne));
    }

    // DELETE /lignes/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        ligneRepository.delete(trouverOu404(ligneRepository, id, "Ligne"));
    }

    // DTO -> Entity
    private void dtoVersEntity(LigneRequestDTO dto, LigneEntity ligne) {
        ligne.setNumero(dto.getNumero());
        ligne.setLibelle(dto.getLibelle());
        ligne.setTerminusDepart(dto.getTerminusDepart());
        ligne.setTerminusArrivee(dto.getTerminusArrivee());
        ligne.setDureeTrajetMinutes(dto.getDureeTrajetMinutes());                                               // F12
        ligne.setTempsRetourMinutes(dto.getTempsRetourMinutes() != null ? dto.getTempsRetourMinutes() : 0);    // F12
    }

    // Entity -> DTO
    private LigneResponseDTO entityVersResponseDTO(LigneEntity ligne) {
        LigneResponseDTO dto = new LigneResponseDTO();
        dto.setId(ligne.getId());
        dto.setNumero(ligne.getNumero());
        dto.setLibelle(ligne.getLibelle());
        dto.setTerminusDepart(ligne.getTerminusDepart());
        dto.setTerminusArrivee(ligne.getTerminusArrivee());
        dto.setDureeTrajetMinutes(ligne.getDureeTrajetMinutes());   // F12
        dto.setTempsRetourMinutes(ligne.getTempsRetourMinutes());   // F12
        return dto;
    }
}
package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.ArretRequestDTO;
import com.projet4.transporturbain.DTO.ArretResponseDTO;
import com.projet4.transporturbain.Entity.ArretEntity;
import com.projet4.transporturbain.Repository.ArretRepository;
import com.projet4.transporturbain.Repository.DesserteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ArretService {

    private final ArretRepository arretRepository;
    private final DesserteRepository desserteRepository;   // F9

    // F9 - GET /arrets?commune= : arrêts de la commune (tous si absente), sans tenir compte des majuscules, triés par nom
    public List<ArretResponseDTO> lister(String commune) {
        List<ArretEntity> arrets = (commune == null || commune.isBlank())
                ? arretRepository.findAllByOrderByNomAsc()
                : arretRepository.findByCommuneIgnoreCaseOrderByNomAsc(commune.trim());
        return arrets.stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /arrets/{id} : récupère un arrêt par son id (404 si introuvable)
    public ArretResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(arretRepository, id, "Arrêt"));
    }

    // POST /arrets : enregistre un nouvel arrêt (INSERT)
    public ArretResponseDTO creer(ArretRequestDTO dto) {
        ArretEntity arret = new ArretEntity();
        dtoVersEntity(dto, arret);
        return entityVersResponseDTO(arretRepository.save(arret));
    }

    // PUT /arrets/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public ArretResponseDTO modifier(Long id, ArretRequestDTO dto) {
        ArretEntity arret = trouverOu404(arretRepository, id, "Arrêt");
        dtoVersEntity(dto, arret);
        return entityVersResponseDTO(arretRepository.save(arret));
    }

    // DELETE /arrets/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        arretRepository.delete(trouverOu404(arretRepository, id, "Arrêt"));
    }

    // DTO -> Entity
    private void dtoVersEntity(ArretRequestDTO dto, ArretEntity arret) {
        arret.setNom(dto.getNom());
        arret.setCommune(dto.getCommune());
        arret.setLatitude(dto.getLatitude());
        arret.setLongitude(dto.getLongitude());
    }

    // Entity -> DTO
    private ArretResponseDTO entityVersResponseDTO(ArretEntity arret) {
        ArretResponseDTO dto = new ArretResponseDTO();
        dto.setId(arret.getId());
        dto.setNom(arret.getNom());
        dto.setCommune(arret.getCommune());
        dto.setLatitude(arret.getLatitude());
        dto.setLongitude(arret.getLongitude());
        dto.setLignes(desserteRepository.findByArretIdOrderByLigneNumeroAsc(arret.getId()).stream()   // F9
                .map(d -> d.getLigne().getNumero())
                .distinct()
                .toList());
        return dto;
    }
}
package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.PaiementRequestDTO;
import com.projet2.microfinance.DTO.PaiementResponseDTO;
import com.projet2.microfinance.Entity.PaiementEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Repository.PaiementRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final PretRepository pretRepository;

    // GET /paiements : récupère l'historique de tous les paiements
    public List<PaiementResponseDTO> lister() {
        return paiementRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /paiements/{id} : récupère un paiement par son id (404 s'il n'existe pas)
    public PaiementResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(paiementRepository, id, "Paiement"));
    }

    // F9 - GET /prets/{id}/paiements : paiements du prêt du plus récent au plus ancien (404 si le prêt n'existe pas)
    public List<PaiementResponseDTO> listerParPret(Long pretId) {
        trouverOu404(pretRepository, pretId, "Prêt");
        return paiementRepository.findByPretIdOrderByDatePaiementDescIdDesc(pretId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /paiements : enregistre un paiement reçu (INSERT), rattaché à son prêt
    public PaiementResponseDTO creer(PaiementRequestDTO dto) {
        PaiementEntity paiement = new PaiementEntity();
        dtoVersEntity(dto, paiement);
        return entityVersResponseDTO(paiementRepository.save(paiement));
    }

    // PUT /paiements/{id} : charge le paiement existant (404 sinon), corrige la saisie, puis UPDATE
    public PaiementResponseDTO modifier(Long id, PaiementRequestDTO dto) {
        PaiementEntity paiement = trouverOu404(paiementRepository, id, "Paiement");
        dtoVersEntity(dto, paiement);
        return entityVersResponseDTO(paiementRepository.save(paiement));
    }

    // DELETE /paiements/{id} : vérifie que le paiement existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        paiementRepository.delete(trouverOu404(paiementRepository, id, "Paiement"));
    }

    // DTO -> Entity (pretId est transformé en objet, 404 s'il n'existe pas)
    private void dtoVersEntity(PaiementRequestDTO dto, PaiementEntity paiement) {
        PretEntity pret = trouverOu404(pretRepository, dto.getPretId(), "Prêt");

        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setMontant(dto.getMontant());
        paiement.setModeReglement(dto.getModeReglement());
        paiement.setPret(pret);
    }

    // Entity -> DTO
    private PaiementResponseDTO entityVersResponseDTO(PaiementEntity paiement) {
        PaiementResponseDTO dto = new PaiementResponseDTO();
        dto.setId(paiement.getId());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setMontant(paiement.getMontant());
        dto.setModeReglement(paiement.getModeReglement());
        dto.setPretId(paiement.getPret().getId());
        return dto;
    }
}

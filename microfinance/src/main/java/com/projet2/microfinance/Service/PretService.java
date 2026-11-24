package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.PretRequestDTO;
import com.projet2.microfinance.DTO.PretResponseDTO;
import com.projet2.microfinance.Entity.ClientEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Repository.ClientRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class PretService {

    private final PretRepository pretRepository;
    private final ClientRepository clientRepository;

    // GET /prets : récupère tout le portefeuille de prêts
    public List<PretResponseDTO> lister() {
        return pretRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /prets/{id} : récupère un prêt par son id (404 s'il n'existe pas)
    public PretResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(pretRepository, id, "Prêt"));
    }

    // POST /prets : enregistre un nouveau prêt (INSERT), rattaché à son client
    public PretResponseDTO creer(PretRequestDTO dto) {
        PretEntity pret = new PretEntity();
        dtoVersEntity(dto, pret);
        return entityVersResponseDTO(pretRepository.save(pret));
    }

    // PUT /prets/{id} : charge le prêt existant (404 sinon), remplace ses champs (dont le statut), puis UPDATE
    public PretResponseDTO modifier(Long id, PretRequestDTO dto) {
        PretEntity pret = trouverOu404(pretRepository, id, "Prêt");
        dtoVersEntity(dto, pret);
        return entityVersResponseDTO(pretRepository.save(pret));
    }

    // DELETE /prets/{id} : vérifie que le prêt existe (404 sinon), puis supprime le dossier
    public void supprimer(Long id) {
        pretRepository.delete(trouverOu404(pretRepository, id, "Prêt"));
    }

    // DTO -> Entity (clientId est transformé en objet, 404 s'il n'existe pas ; statut absent = inchangé)
    private void dtoVersEntity(PretRequestDTO dto, PretEntity pret) {
        ClientEntity client = trouverOu404(clientRepository, dto.getClientId(), "Client");

        pret.setMontant(dto.getMontant());
        pret.setDureeMois(dto.getDureeMois());
        pret.setTauxAnnuel(dto.getTauxAnnuel());
        pret.setDateAccord(dto.getDateAccord());
        if (dto.getStatut() != null) {
            pret.setStatut(dto.getStatut());
        }
        pret.setAgent(dto.getAgent());
        pret.setClient(client);
    }

    // Entity -> DTO
    private PretResponseDTO entityVersResponseDTO(PretEntity pret) {
        PretResponseDTO dto = new PretResponseDTO();
        dto.setId(pret.getId());
        dto.setMontant(pret.getMontant());
        dto.setDureeMois(pret.getDureeMois());
        dto.setTauxAnnuel(pret.getTauxAnnuel());
        dto.setDateAccord(pret.getDateAccord());
        dto.setStatut(pret.getStatut());
        dto.setAgent(pret.getAgent());
        dto.setClientId(pret.getClient().getId());
        dto.setClientNom(pret.getClient().getNom());
        return dto;
    }
}

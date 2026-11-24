package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.CompteRequestDTO;
import com.projet2.microfinance.DTO.CompteResponseDTO;
import com.projet2.microfinance.Entity.ClientEntity;
import com.projet2.microfinance.Entity.CompteEntity;
import com.projet2.microfinance.Repository.ClientRepository;
import com.projet2.microfinance.Repository.CompteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class CompteService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;

    // GET /comptes : récupère tous les comptes
    public List<CompteResponseDTO> lister() {
        return compteRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /comptes/{id} : récupère un compte par son id (404 s'il n'existe pas)
    public CompteResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(compteRepository, id, "Compte"));
    }

    // F6 - GET /clients/{id}/comptes : comptes d'un client triés par date d'ouverture (404 si le client n'existe pas)
    public List<CompteResponseDTO> listerParClient(Long clientId) {
        trouverOu404(clientRepository, clientId, "Client");
        return compteRepository.findByClientIdOrderByDateOuvertureAsc(clientId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /comptes : ouvre un nouveau compte (INSERT), rattaché à son client
    public CompteResponseDTO creer(CompteRequestDTO dto) {
        CompteEntity compte = new CompteEntity();
        dtoVersEntity(dto, compte);
        return entityVersResponseDTO(compteRepository.save(compte));
    }

    // PUT /comptes/{id} : charge le compte existant (404 sinon), remplace ses champs, puis UPDATE
    public CompteResponseDTO modifier(Long id, CompteRequestDTO dto) {
        CompteEntity compte = trouverOu404(compteRepository, id, "Compte");
        dtoVersEntity(dto, compte);
        return entityVersResponseDTO(compteRepository.save(compte));
    }

    // DELETE /comptes/{id} : vérifie que le compte existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        compteRepository.delete(trouverOu404(compteRepository, id, "Compte"));
    }

    // DTO -> Entity (clientId est transformé en objet, 404 s'il n'existe pas)
    private void dtoVersEntity(CompteRequestDTO dto, CompteEntity compte) {
        ClientEntity client = trouverOu404(clientRepository, dto.getClientId(), "Client");

        compte.setNumero(dto.getNumero());
        compte.setType(dto.getType());
        compte.setDateOuverture(dto.getDateOuverture());
        compte.setSolde(dto.getSolde());
        compte.setClient(client);
    }

    // Entity -> DTO
    private CompteResponseDTO entityVersResponseDTO(CompteEntity compte) {
        CompteResponseDTO dto = new CompteResponseDTO();
        dto.setId(compte.getId());
        dto.setNumero(compte.getNumero());
        dto.setType(compte.getType());
        dto.setDateOuverture(compte.getDateOuverture());
        dto.setSolde(compte.getSolde());
        dto.setClientId(compte.getClient().getId());
        dto.setClientNom(compte.getClient().getNom());
        return dto;
    }
}

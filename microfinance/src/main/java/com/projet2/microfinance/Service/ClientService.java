package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.ClientRequestDTO;
import com.projet2.microfinance.DTO.ClientResponseDTO;
import com.projet2.microfinance.Entity.ClientEntity;
import com.projet2.microfinance.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    // GET /clients : récupère tous les clients
    public List<ClientResponseDTO> lister() {
        return clientRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /clients/{id} : récupère un client par son id (404 s'il n'existe pas)
    public ClientResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(clientRepository, id, "Client"));
    }

    // POST /clients : crée une nouvelle fiche client (INSERT)
    public ClientResponseDTO creer(ClientRequestDTO dto) {
        ClientEntity client = new ClientEntity();
        dtoVersEntity(dto, client);
        return entityVersResponseDTO(clientRepository.save(client));
    }

    // PUT /clients/{id} : charge le client existant (404 sinon), remplace ses champs, puis UPDATE
    public ClientResponseDTO modifier(Long id, ClientRequestDTO dto) {
        ClientEntity client = trouverOu404(clientRepository, id, "Client");
        dtoVersEntity(dto, client);
        return entityVersResponseDTO(clientRepository.save(client));
    }

    // DELETE /clients/{id} : vérifie que le client existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        clientRepository.delete(trouverOu404(clientRepository, id, "Client"));
    }

    // DTO -> Entity
    private void dtoVersEntity(ClientRequestDTO dto, ClientEntity client) {
        client.setNom(dto.getNom());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setPlafondEmprunt(dto.getPlafondEmprunt());
        client.setAgentCredit(dto.getAgentCredit());
    }

    // Entity -> DTO
    private ClientResponseDTO entityVersResponseDTO(ClientEntity client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setTelephone(client.getTelephone());
        dto.setAdresse(client.getAdresse());
        dto.setPlafondEmprunt(client.getPlafondEmprunt());
        dto.setAgentCredit(client.getAgentCredit());
        return dto;
    }
}

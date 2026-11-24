package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.ClientRequestDTO;
import com.projet1.placedemarche.DTO.ClientResponseDTO;
import com.projet1.placedemarche.Entity.ClientEntity;
import com.projet1.placedemarche.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    // GET /clients : récupère tous les clients et les convertit en DTO
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

    // POST /clients : crée un nouveau client (INSERT) et le renvoie avec son id
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
        client.setEmail(dto.getEmail());
        client.setAdresseLivraison(dto.getAdresseLivraison());
    }

    // Entity -> DTO
    private ClientResponseDTO entityVersResponseDTO(ClientEntity client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setEmail(client.getEmail());
        dto.setAdresseLivraison(client.getAdresseLivraison());
        return dto;
    }
}

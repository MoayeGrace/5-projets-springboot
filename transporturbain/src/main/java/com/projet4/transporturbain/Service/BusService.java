package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.BusRequestDTO;
import com.projet4.transporturbain.DTO.BusResponseDTO;
import com.projet4.transporturbain.Entity.BusEntity;
import com.projet4.transporturbain.Repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    // GET /bus : récupère tout le parc de bus
    public List<BusResponseDTO> lister() {
        return busRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /bus/{id} : récupère un bus par son id (404 si introuvable)
    public BusResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(busRepository, id, "Bus"));
    }

    // POST /bus : enregistre un nouveau bus (INSERT)
    public BusResponseDTO creer(BusRequestDTO dto) {
        BusEntity bus = new BusEntity();
        dtoVersEntity(dto, bus);
        return entityVersResponseDTO(busRepository.save(bus));
    }

    // PUT /bus/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public BusResponseDTO modifier(Long id, BusRequestDTO dto) {
        BusEntity bus = trouverOu404(busRepository, id, "Bus");
        dtoVersEntity(dto, bus);
        return entityVersResponseDTO(busRepository.save(bus));
    }

    // DELETE /bus/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        busRepository.delete(trouverOu404(busRepository, id, "Bus"));
    }

    // DTO -> Entity
    private void dtoVersEntity(BusRequestDTO dto, BusEntity bus) {
        bus.setImmatriculation(dto.getImmatriculation());
        bus.setCapacite(dto.getCapacite());
        bus.setDepot(dto.getDepot());
    }

    // Entity -> DTO
    private BusResponseDTO entityVersResponseDTO(BusEntity bus) {
        BusResponseDTO dto = new BusResponseDTO();
        dto.setId(bus.getId());
        dto.setImmatriculation(bus.getImmatriculation());
        dto.setCapacite(bus.getCapacite());
        dto.setDepot(bus.getDepot());
        return dto;
    }
}
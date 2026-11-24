package com.projet3.atelierdeproduction.Service;

import com.projet3.atelierdeproduction.DTO.MachineRequestDTO;
import com.projet3.atelierdeproduction.DTO.MachineResponseDTO;
import com.projet3.atelierdeproduction.Entity.MachineEntity;
import com.projet3.atelierdeproduction.Repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet3.atelierdeproduction.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;

    // GET /machines : récupère tout le parc de machines
    public List<MachineResponseDTO> lister() {
        return machineRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /machines/{id} : récupère la fiche d'une machine (404 si elle n'existe pas)
    public MachineResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(machineRepository, id, "Machine"));
    }

    // POST /machines : enregistre une nouvelle machine (INSERT)
    public MachineResponseDTO creer(MachineRequestDTO dto) {
        MachineEntity machine = new MachineEntity();
        dtoVersEntity(dto, machine);
        return entityVersResponseDTO(machineRepository.save(machine));
    }

    // PUT /machines/{id} : charge la machine existante (404 sinon), remplace ses champs, puis UPDATE
    public MachineResponseDTO modifier(Long id, MachineRequestDTO dto) {
        MachineEntity machine = trouverOu404(machineRepository, id, "Machine");
        dtoVersEntity(dto, machine);
        return entityVersResponseDTO(machineRepository.save(machine));
    }

    // DELETE /machines/{id} : vérifie que la machine existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        machineRepository.delete(trouverOu404(machineRepository, id, "Machine"));
    }

    // DTO -> Entity
    private void dtoVersEntity(MachineRequestDTO dto, MachineEntity machine) {
        machine.setCode(dto.getCode());
        machine.setType(dto.getType());
        machine.setAtelier(dto.getAtelier());
    }

    // Entity -> DTO
    private MachineResponseDTO entityVersResponseDTO(MachineEntity machine) {
        MachineResponseDTO dto = new MachineResponseDTO();
        dto.setId(machine.getId());
        dto.setCode(machine.getCode());
        dto.setType(machine.getType());
        dto.setAtelier(machine.getAtelier());
        return dto;
    }
}

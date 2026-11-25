package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.BulletinRequestDTO;
import com.projet5.lapaie.DTO.BulletinResponseDTO;
import com.projet5.lapaie.Entity.BulletinEntity;
import com.projet5.lapaie.Entity.EmployeEntity;
import com.projet5.lapaie.Repository.BulletinRepository;
import com.projet5.lapaie.Repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.projet5.lapaie.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class BulletinService {

    private final BulletinRepository bulletinRepository;
    private final EmployeRepository employeRepository;

    // GET /bulletins : récupère tous les bulletins
    public List<BulletinResponseDTO> lister() {
        return bulletinRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /bulletins/{id} : récupère un bulletin par son id (404 si introuvable)
    public BulletinResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(bulletinRepository, id, "Bulletin"));
    }

    // F7 - GET /employes/{id}/bulletins?annee= : bulletins de l'année (en cours par défaut), du mois le plus récent au plus ancien
    public List<BulletinResponseDTO> listerParEmploye(Long employeId, Integer annee) {
        trouverOu404(employeRepository, employeId, "Employé");
        int an = (annee != null) ? annee : LocalDate.now().getYear();
        return bulletinRepository.findByEmployeIdAndMoisStartingWithOrderByMoisDesc(employeId, an + "-")
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /bulletins : établit un nouveau bulletin (INSERT)
    public BulletinResponseDTO creer(BulletinRequestDTO dto) {
        BulletinEntity bulletin = new BulletinEntity();
        dtoVersEntity(dto, bulletin);
        return entityVersResponseDTO(bulletinRepository.save(bulletin));
    }

    // PUT /bulletins/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public BulletinResponseDTO modifier(Long id, BulletinRequestDTO dto) {
        BulletinEntity bulletin = trouverOu404(bulletinRepository, id, "Bulletin");
        dtoVersEntity(dto, bulletin);
        return entityVersResponseDTO(bulletinRepository.save(bulletin));
    }

    // DELETE /bulletins/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        bulletinRepository.delete(trouverOu404(bulletinRepository, id, "Bulletin"));
    }

    // DTO -> Entity (employeId est transformé en objet, 404 s'il n'existe pas)
    private void dtoVersEntity(BulletinRequestDTO dto, BulletinEntity bulletin) {
        EmployeEntity employe = trouverOu404(employeRepository, dto.getEmployeId(), "Employé");

        bulletin.setMois(dto.getMois());
        bulletin.setBrut(dto.getBrut());
        bulletin.setNet(dto.getNet());
        bulletin.setEmploye(employe);
    }

    // Entity -> DTO
    private BulletinResponseDTO entityVersResponseDTO(BulletinEntity bulletin) {
        BulletinResponseDTO dto = new BulletinResponseDTO();
        dto.setId(bulletin.getId());
        dto.setMois(bulletin.getMois());
        dto.setBrut(bulletin.getBrut());
        dto.setNet(bulletin.getNet());
        dto.setEdite(bulletin.getEdite());
        dto.setEmployeId(bulletin.getEmploye().getId());
        dto.setEmployeNom(bulletin.getEmploye().getNom());
        return dto;
    }
}
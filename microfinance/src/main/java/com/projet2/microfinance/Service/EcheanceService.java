package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.EcheanceRequestDTO;
import com.projet2.microfinance.DTO.EcheanceResponseDTO;
import com.projet2.microfinance.DTO.EcheancierDTO;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Repository.EcheanceRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class EcheanceService {

    private final EcheanceRepository echeanceRepository;
    private final PretRepository pretRepository;

    // GET /echeances : récupère toutes les échéances
    public List<EcheanceResponseDTO> lister() {
        return echeanceRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /echeances/{id} : récupère une échéance par son id (404 si elle n'existe pas)
    public EcheanceResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(echeanceRepository, id, "Échéance"));
    }

    // F8 - GET /prets/{id}/echeances : échéancier du prêt trié par date (404 si le prêt n'existe pas, "non généré" s'il est vide)
    public EcheancierDTO echeancierDuPret(Long pretId) {
        trouverOu404(pretRepository, pretId, "Prêt");
        List<EcheanceResponseDTO> echeances = echeanceRepository.findByPretIdOrderByDateEcheanceAscNumeroAsc(pretId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();

        EcheancierDTO dto = new EcheancierDTO();
        dto.setPretId(pretId);
        dto.setGenere(!echeances.isEmpty());
        dto.setMessage(echeances.isEmpty() ? "L'échéancier de ce prêt n'est pas encore généré" : null);
        dto.setEcheances(echeances);
        return dto;
    }

    // POST /echeances : saisit une nouvelle échéance (INSERT), rattachée à son prêt
    public EcheanceResponseDTO creer(EcheanceRequestDTO dto) {
        EcheanceEntity echeance = new EcheanceEntity();
        dtoVersEntity(dto, echeance);
        return entityVersResponseDTO(echeanceRepository.save(echeance));
    }

    // PUT /echeances/{id} : charge l'échéance existante (404 sinon), corrige ses montants, puis UPDATE
    public EcheanceResponseDTO modifier(Long id, EcheanceRequestDTO dto) {
        EcheanceEntity echeance = trouverOu404(echeanceRepository, id, "Échéance");
        dtoVersEntity(dto, echeance);
        return entityVersResponseDTO(echeanceRepository.save(echeance));
    }

    // DELETE /echeances/{id} : vérifie que l'échéance existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        echeanceRepository.delete(trouverOu404(echeanceRepository, id, "Échéance"));
    }

    // DTO -> Entity (pretId est transformé en objet, 404 s'il n'existe pas ; le total est calculé)
    private void dtoVersEntity(EcheanceRequestDTO dto, EcheanceEntity echeance) {
        PretEntity pret = trouverOu404(pretRepository, dto.getPretId(), "Prêt");

        echeance.setNumero(dto.getNumero());
        echeance.setDateEcheance(dto.getDateEcheance());
        echeance.setMontantCapital(dto.getMontantCapital());
        echeance.setMontantInterets(dto.getMontantInterets());
        echeance.setMontantTotal(dto.getMontantCapital().add(dto.getMontantInterets()));
        echeance.setPret(pret);
    }

    // Entity -> DTO
    private EcheanceResponseDTO entityVersResponseDTO(EcheanceEntity echeance) {
        EcheanceResponseDTO dto = new EcheanceResponseDTO();
        dto.setId(echeance.getId());
        dto.setNumero(echeance.getNumero());
        dto.setDateEcheance(echeance.getDateEcheance());
        dto.setMontantCapital(echeance.getMontantCapital());
        dto.setMontantInterets(echeance.getMontantInterets());
        dto.setMontantTotal(echeance.getMontantTotal());
        dto.setMontantPaye(echeance.montantPaye());
        dto.setResteAPayer(echeance.resteAPayer());
        dto.setPretId(echeance.getPret().getId());
        return dto;
    }
}

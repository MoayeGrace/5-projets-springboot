package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.PageResponseDTO;
import com.projet2.microfinance.DTO.PretRequestDTO;
import com.projet2.microfinance.DTO.PretResponseDTO;
import com.projet2.microfinance.Entity.ClientEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import com.projet2.microfinance.Repository.ClientRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static com.projet2.microfinance.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class PretService {

    private final PretRepository pretRepository;
    private final ClientRepository clientRepository;

    // F10 - taille de page maximale autorisée
    private static final int TAILLE_PAGE_MAX = 100;

    // F12 - nombre maximal de prêts actifs (non soldés) par client
    private static final int MAX_PRETS_ACTIFS = 3;

    // F10 - GET /prets?statut=&page=&size=&sort= : portefeuille filtré par statut (tous si absent), page par page
    public PageResponseDTO<PretResponseDTO> lister(StatutPret statut, Pageable pageable) {
        // F10 - une taille trop grande est ramenée au maximum (on garde le numéro de page et le tri demandés)
        if (pageable.getPageSize() > TAILLE_PAGE_MAX) {
            pageable = PageRequest.of(pageable.getPageNumber(), TAILLE_PAGE_MAX, pageable.getSort());
        }
        Page<PretEntity> page = (statut == null)
                ? pretRepository.findAll(pageable)
                : pretRepository.findByStatut(statut, pageable);
        return new PageResponseDTO<>(page.map(this::entityVersResponseDTO));
    }

    // GET /prets/{id} : récupère un prêt par son id (404 s'il n'existe pas)
    public PretResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(pretRepository, id, "Prêt"));
    }

    // F7 - GET /clients/{id}/prets : prêts d'un client du plus récent au plus ancien (404 si le client n'existe pas)
    public List<PretResponseDTO> listerParClient(Long clientId) {
        trouverOu404(clientRepository, clientId, "Client");
        return pretRepository.findByClientIdOrderByDateAccordDescIdDesc(clientId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /prets : enregistre un nouveau prêt (INSERT), rattaché à son client
    public PretResponseDTO creer(PretRequestDTO dto) {
        verifierLimites(dto);   // F12
        PretEntity pret = new PretEntity();
        dtoVersEntity(dto, pret);
        return entityVersResponseDTO(pretRepository.save(pret));
    }

    // PUT /prets/{id} : charge le prêt existant (404 sinon), remplace ses champs (dont le statut), puis UPDATE
    public PretResponseDTO modifier(Long id, PretRequestDTO dto) {
        PretEntity pret = trouverOu404(pretRepository, id, "Prêt");
        verifierParametresFiges(pret, dto);   // F11
        dtoVersEntity(dto, pret);
        return entityVersResponseDTO(pretRepository.save(pret));
    }

    // F12 - au plus 3 prêts actifs par client, et un total emprunté (prêts actifs + nouveau) qui ne dépasse pas son plafond (409)
    private void verifierLimites(PretRequestDTO dto) {
        ClientEntity client = trouverOu404(clientRepository, dto.getClientId(), "Client");
        long pretsActifs = pretRepository.countByClientIdAndStatutNot(client.getId(), StatutPret.SOLDE);
        if (pretsActifs >= MAX_PRETS_ACTIFS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le client a déjà " + pretsActifs + " prêts actifs (maximum " + MAX_PRETS_ACTIFS + ")");
        }
        BigDecimal dejaEmprunte = pretRepository.sommeMontantsSaufStatut(client.getId(), StatutPret.SOLDE);
        if (dejaEmprunte.add(dto.getMontant()).compareTo(client.getPlafondEmprunt()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Plafond atteint : " + pretsActifs + " prêt(s) actif(s) pour " + dejaEmprunte
                            + ", + " + dto.getMontant() + " dépasse le plafond de " + client.getPlafondEmprunt());
        }
    }

    // F11 - une fois l'échéancier généré, le montant, la durée, le taux et la date d'accord ne peuvent plus changer (409)
    private void verifierParametresFiges(PretEntity pret, PretRequestDTO dto) {
        if (pret.getEcheances().isEmpty()) {
            return;
        }
        boolean modifie = pret.getMontant().compareTo(dto.getMontant()) != 0
                || !pret.getDureeMois().equals(dto.getDureeMois())
                || pret.getTauxAnnuel().compareTo(dto.getTauxAnnuel()) != 0
                || !pret.getDateAccord().equals(dto.getDateAccord());
        if (modifie) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "L'échéancier est généré : le montant, la durée, le taux et la date d'accord sont figés");
        }
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
        dto.setCapitalRestantDu(pret.capitalRestantDu());           // F7
        dto.setSolde(pret.getStatut() == StatutPret.SOLDE);         // F7
        dto.setDateSolde(pret.getDateSolde());                      // F15
        dto.setJoursRetardMax(pret.getJoursRetardMax());            // F16
        dto.setClientId(pret.getClient().getId());
        dto.setClientNom(pret.getClient().getNom());
        return dto;
    }
}

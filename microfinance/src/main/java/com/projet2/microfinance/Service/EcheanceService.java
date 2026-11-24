package com.projet2.microfinance.Service;

import com.projet2.microfinance.DTO.EcheanceRequestDTO;
import com.projet2.microfinance.DTO.EcheanceResponseDTO;
import com.projet2.microfinance.DTO.EcheancierDTO;
import com.projet2.microfinance.Entity.EcheanceEntity;
import com.projet2.microfinance.Entity.PretEntity;
import com.projet2.microfinance.Entity.StatutPret;
import com.projet2.microfinance.Repository.EcheanceRepository;
import com.projet2.microfinance.Repository.PretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

    // F11 - POST /prets/{id}/echeancier : une échéance par mois à mensualité constante ; la dernière ajuste le capital
    // pour que la somme des capitaux égale exactement le montant emprunté. 409 si déjà généré, 400 si le prêt n'est pas ACCORDE
    @Transactional
    public EcheancierDTO genererEcheancier(Long pretId) {
        PretEntity pret = trouverOu404(pretRepository, pretId, "Prêt");
        if (!pret.getEcheances().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'échéancier de ce prêt existe déjà");
        }
        if (pret.getStatut() != StatutPret.ACCORDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'échéancier ne peut être généré que pour un prêt au statut ACCORDE (statut actuel : " + pret.getStatut() + ")");
        }

        int duree = pret.getDureeMois();
        BigDecimal tauxMensuel = pret.getTauxAnnuel().divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal mensualite = calculerMensualite(pret.getMontant(), tauxMensuel, duree);
        BigDecimal capitalRestant = pret.getMontant();

        for (int numero = 1; numero <= duree; numero++) {
            BigDecimal interets = capitalRestant.multiply(tauxMensuel).setScale(2, RoundingMode.HALF_UP);
            // F11 - dernière échéance : on rembourse tout le capital restant (absorbe les écarts d'arrondi)
            BigDecimal capital = (numero == duree)
                    ? capitalRestant
                    : mensualite.subtract(interets).min(capitalRestant);

            EcheanceEntity echeance = new EcheanceEntity();
            echeance.setNumero(numero);
            echeance.setDateEcheance(pret.getDateAccord().plusMonths(numero));
            echeance.setMontantCapital(capital);
            echeance.setMontantInterets(interets);
            echeance.setMontantTotal(capital.add(interets));
            echeance.setPret(pret);
            pret.getEcheances().add(echeanceRepository.save(echeance));

            capitalRestant = capitalRestant.subtract(capital);
        }

        pret.setStatut(StatutPret.EN_COURS);   // F11 - le prêt passe EN_COURS, ses paramètres sont désormais figés
        return echeancierDuPret(pretId);
    }

    // F11 - mensualité constante : M = C × t / (1 - (1 + t)^-n), ou C / n si le taux est nul
    private BigDecimal calculerMensualite(BigDecimal montant, BigDecimal tauxMensuel, int duree) {
        if (tauxMensuel.signum() == 0) {
            return montant.divide(BigDecimal.valueOf(duree), 2, RoundingMode.HALF_UP);
        }
        BigDecimal facteur = BigDecimal.ONE.add(tauxMensuel).pow(duree, MathContext.DECIMAL64);   // (1 + t)^n
        return montant.multiply(tauxMensuel).multiply(facteur)
                .divide(facteur.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
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

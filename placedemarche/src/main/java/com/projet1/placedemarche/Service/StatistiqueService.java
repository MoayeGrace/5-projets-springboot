package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.ChiffreAffairesDTO;
import com.projet1.placedemarche.DTO.TopProduitDTO;
import com.projet1.placedemarche.Entity.CommandeEntity;
import com.projet1.placedemarche.Entity.StatutCommande;
import com.projet1.placedemarche.Repository.CommandeRepository;
import com.projet1.placedemarche.Repository.VendeurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final CommandeRepository commandeRepository;
    private final VendeurRepository vendeurRepository;

    // F17 - GET /vendeurs/{id}/chiffre-affaires?mois=2026-11 : somme des commandes LIVRÉES dans le mois,
    // calculée avec le prix figé à la commande (F15) ; commandes annulées exclues ; 0 si aucune vente
    public ChiffreAffairesDTO chiffreAffaires(Long vendeurId, YearMonth mois) {
        trouverOu404(vendeurRepository, vendeurId, "Vendeur");
        YearMonth moisCalcule = (mois != null) ? mois : YearMonth.now();   // sans paramètre : le mois en cours

        List<CommandeEntity> livrees = commandeRepository.livreesParVendeur(vendeurId, StatutCommande.LIVREE,
                moisCalcule.atDay(1).atStartOfDay(), moisCalcule.plusMonths(1).atDay(1).atStartOfDay());

        ChiffreAffairesDTO resultat = new ChiffreAffairesDTO();
        resultat.setVendeurId(vendeurId);
        resultat.setMois(moisCalcule.toString());
        resultat.setNombreCommandes(livrees.size());

        // F17 - détail par produit : on cumule quantité et montant de chaque commande
        Map<Long, ChiffreAffairesDTO.Ligne> parProduit = new LinkedHashMap<>();
        for (CommandeEntity c : livrees) {
            BigDecimal montant = c.getPrixUnitaire().multiply(BigDecimal.valueOf(c.getQuantite()));
            resultat.setTotal(resultat.getTotal().add(montant));

            ChiffreAffairesDTO.Ligne ligne = parProduit.computeIfAbsent(c.getProduit().getId(), idProduit -> {
                ChiffreAffairesDTO.Ligne nouvelle = new ChiffreAffairesDTO.Ligne();
                nouvelle.setProduitId(idProduit);
                nouvelle.setProduitNom(c.getProduit().getNom());
                return nouvelle;
            });
            ligne.setQuantite(ligne.getQuantite() + c.getQuantite());
            ligne.setMontant(ligne.getMontant().add(montant));
        }
        parProduit.values().stream()
                .sorted(Comparator.comparing(ChiffreAffairesDTO.Ligne::getMontant).reversed())
                .forEach(resultat.getDetail()::add);
        return resultat;
    }

    // F18 - GET /produits/top?limite=10&du=...&au=... : produits les plus commandés sur la période, hors annulées.
    // Sans période : le mois en cours ; limite par défaut 10, plafonnée à 50
    public List<TopProduitDTO> topProduits(Integer limite, LocalDate du, LocalDate au) {
        int limiteCalculee = (limite == null) ? 10 : Math.min(Math.max(limite, 1), 50);
        LocalDate debut = (du != null) ? du : YearMonth.now().atDay(1);
        LocalDate fin = (au != null) ? au : YearMonth.now().atEndOfMonth();
        if (debut.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
        }
        LocalDateTime debutInclus = debut.atStartOfDay();
        LocalDateTime finExclue = fin.plusDays(1).atStartOfDay();   // "au" est inclus : on va jusqu'au lendemain 0h00
        return commandeRepository.topProduits(StatutCommande.ANNULEE, debutInclus, finExclue,
                PageRequest.of(0, limiteCalculee));
    }
}

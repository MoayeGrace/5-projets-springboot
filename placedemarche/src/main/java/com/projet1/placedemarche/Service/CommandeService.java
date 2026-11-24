package com.projet1.placedemarche.Service;

import com.projet1.placedemarche.DTO.CommandeRequestDTO;
import com.projet1.placedemarche.DTO.CommandeResponseDTO;
import com.projet1.placedemarche.Entity.ClientEntity;
import com.projet1.placedemarche.Entity.CommandeEntity;
import com.projet1.placedemarche.Entity.ProduitEntity;
import com.projet1.placedemarche.Repository.ClientRepository;
import com.projet1.placedemarche.Repository.CommandeRepository;
import com.projet1.placedemarche.Repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static com.projet1.placedemarche.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;

    // GET /commandes : récupère toutes les commandes et les convertit en DTO
    public List<CommandeResponseDTO> lister() {
        return commandeRepository.findAll()
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // GET /commandes/{id} : récupère une commande par son id (404 si elle n'existe pas)
    public CommandeResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(commandeRepository, id, "Commande"));
    }

    // F8 - GET /clients/{id}/commandes : historique d'un client, du plus récent au plus ancien (404 si le client n'existe pas)
    public List<CommandeResponseDTO> listerParClient(Long clientId) {
        trouverOu404(clientRepository, clientId, "Client");
        return commandeRepository.findByClientIdOrderByDateCommandeDesc(clientId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // POST /commandes + F11 (stock suffisant) + F12 (stock décrémenté, même transaction, ligne verrouillée) + F15 (prix figé)
    // @Transactional : si une étape échoue, le stock n'est pas modifié et la commande n'est pas créée
    @Transactional
    public CommandeResponseDTO creer(CommandeRequestDTO dto) {
        ClientEntity client = trouverOu404(clientRepository, dto.getClientId(), "Client");
        ProduitEntity produit = produitVerrouille(dto.getProduitId());

        reserverStock(produit, dto.getQuantite());

        CommandeEntity commande = new CommandeEntity();
        commande.setClient(client);
        commande.setProduit(produit);
        commande.setQuantite(dto.getQuantite());
        commande.setPrixUnitaire(produit.getPrix());   // F15 - le prix du jour est copié et ne bougera plus
        if (dto.getStatut() != null) {
            commande.setStatut(dto.getStatut());
        }
        return entityVersResponseDTO(commandeRepository.save(commande));
    }

    // PUT /commandes/{id} + F11/F12 : l'ancienne quantité est rendue au stock puis la nouvelle est réservée,
    // dans la même transaction (si le stock manque, tout est annulé)
    @Transactional
    public CommandeResponseDTO modifier(Long id, CommandeRequestDTO dto) {
        CommandeEntity commande = trouverOu404(commandeRepository, id, "Commande");
        ClientEntity client = trouverOu404(clientRepository, dto.getClientId(), "Client");

        // F12 - on rend d'abord l'ancienne quantité à l'ancien produit
        ProduitEntity ancienProduit = produitVerrouille(commande.getProduit().getId());
        ancienProduit.modifierStock(ancienProduit.getStock() + commande.getQuantite());

        // F11 + F12 - puis on réserve la nouvelle quantité sur le (nouveau) produit
        ProduitEntity nouveauProduit = produitVerrouille(dto.getProduitId());
        reserverStock(nouveauProduit, dto.getQuantite());

        // F15 - le prix n'est recopié que si le produit change, sinon il reste figé
        if (!ancienProduit.getId().equals(nouveauProduit.getId())) {
            commande.setPrixUnitaire(nouveauProduit.getPrix());
        }
        commande.setClient(client);
        commande.setProduit(nouveauProduit);
        commande.setQuantite(dto.getQuantite());
        if (dto.getStatut() != null) {
            commande.setStatut(dto.getStatut());
        }
        return entityVersResponseDTO(commandeRepository.save(commande));
    }

    // DELETE /commandes/{id} : vérifie que la commande existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        commandeRepository.delete(trouverOu404(commandeRepository, id, "Commande"));
    }

    // ---------- Méthodes privées ----------

    // F12 - charge le produit en verrouillant sa ligne jusqu'à la fin de la transaction (404 s'il n'existe pas)
    private ProduitEntity produitVerrouille(Long produitId) {
        return produitRepository.findByIdAvecVerrou(produitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit " + produitId + " introuvable"));
    }

    // F11 - refuse si la quantité dépasse le stock (409 avec le stock restant) ; F12 - sinon décrémente le stock
    private void reserverStock(ProduitEntity produit, int quantite) {
        if (produit.getStock() == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le produit « " + produit.getNom() + " » est en rupture de stock");
        }
        if (quantite > produit.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuffisant pour « " + produit.getNom() + " » : " + produit.getStock() + " restant(s), " + quantite + " demandé(s)");
        }
        produit.modifierStock(produit.getStock() - quantite);
    }

    // Entity -> DTO (le montant = prix unitaire x quantité est calculé ici)
    private CommandeResponseDTO entityVersResponseDTO(CommandeEntity commande) {
        CommandeResponseDTO dto = new CommandeResponseDTO();
        dto.setId(commande.getId());
        dto.setClientId(commande.getClient().getId());
        dto.setClientNom(commande.getClient().getNom());
        dto.setProduitId(commande.getProduit().getId());
        dto.setProduitNom(commande.getProduit().getNom());
        dto.setQuantite(commande.getQuantite());
        dto.setPrixUnitaire(commande.getPrixUnitaire());
        dto.setMontant(commande.getPrixUnitaire().multiply(BigDecimal.valueOf(commande.getQuantite())));
        dto.setStatut(commande.getStatut());
        dto.setDateCommande(commande.getDateCommande());
        dto.setDateModification(commande.getDateModification());
        return dto;
    }
}

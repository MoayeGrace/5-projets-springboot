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

    // POST /commandes : crée une nouvelle commande (INSERT), avec le prix copié depuis le produit
    public CommandeResponseDTO creer(CommandeRequestDTO dto) {
        CommandeEntity commande = new CommandeEntity();
        dtoVersEntity(dto, commande);
        return entityVersResponseDTO(commandeRepository.save(commande));
    }

    // PUT /commandes/{id} : charge la commande existante (404 sinon), remplace ses champs, puis UPDATE
    public CommandeResponseDTO modifier(Long id, CommandeRequestDTO dto) {
        CommandeEntity commande = trouverOu404(commandeRepository, id, "Commande");
        dtoVersEntity(dto, commande);
        return entityVersResponseDTO(commandeRepository.save(commande));
    }

    // DELETE /commandes/{id} : vérifie que la commande existe (404 sinon), puis la supprime
    public void supprimer(Long id) {
        commandeRepository.delete(trouverOu404(commandeRepository, id, "Commande"));
    }

    // DTO -> Entity
    private void dtoVersEntity(CommandeRequestDTO dto, CommandeEntity commande) {
        ClientEntity client = trouverOu404(clientRepository, dto.getClientId(), "Client");
        ProduitEntity produit = trouverOu404(produitRepository, dto.getProduitId(), "Produit");
        verifierStock(produit, dto.getQuantite());   // F11 - la quantité commandée ne doit pas dépasser le stock

        // F15 : prix copié seulement pour une nouvelle commande ou un changement de produit, sinon il reste figé
        boolean produitChange = commande.getProduit() == null
                || !commande.getProduit().getId().equals(produit.getId());
        if (produitChange) {
            commande.setPrixUnitaire(produit.getPrix());
        }

        commande.setClient(client);
        commande.setProduit(produit);
        commande.setQuantite(dto.getQuantite());

        // Statut facultatif : si absent, on garde l'actuel (EN_ATTENTE pour une nouvelle commande)
        if (dto.getStatut() != null) {
            commande.setStatut(dto.getStatut());
        }
    }

    // F11 - refuse la commande si le produit est en rupture ou si la quantité dépasse le stock (409 avec le stock restant)
    private void verifierStock(ProduitEntity produit, int quantite) {
        if (produit.getStock() == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le produit « " + produit.getNom() + " » est en rupture de stock");
        }
        if (quantite > produit.getStock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuffisant pour « " + produit.getNom() + " » : " + produit.getStock() + " restant(s), " + quantite + " demandé(s)");
        }
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

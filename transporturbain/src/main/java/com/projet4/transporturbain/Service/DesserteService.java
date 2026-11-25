package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.DesserteRequestDTO;
import com.projet4.transporturbain.DTO.DesserteResponseDTO;
import com.projet4.transporturbain.Entity.ArretEntity;
import com.projet4.transporturbain.Entity.DesserteEntity;
import com.projet4.transporturbain.Entity.LigneEntity;
import com.projet4.transporturbain.Repository.ArretRepository;
import com.projet4.transporturbain.Repository.DesserteRepository;
import com.projet4.transporturbain.Repository.LigneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

// F6 / F11 - tracé des lignes : quels arrêts, dans quel ordre
@Service
@RequiredArgsConstructor
public class DesserteService {

    private final DesserteRepository desserteRepository;
    private final LigneRepository ligneRepository;
    private final ArretRepository arretRepository;   // F11

    // F6 - GET /lignes/{id}/arrets : arrêts dans l'ordre de desserte, sens par sens (vide si aucun, 404 si ligne inconnue)
    public List<DesserteResponseDTO> arretsDeLaLigne(Long ligneId) {
        trouverOu404(ligneRepository, ligneId, "Ligne");
        return desserteRepository.findByLigneIdOrderBySensAscPositionAsc(ligneId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F11 - POST /lignes/{id}/arrets : place un arrêt à une position dans un sens ; un arrêt ne figure qu'une fois par
    // ligne et par sens (409) ; une insertion à une position occupée décale d'un rang les arrêts suivants
    @Transactional
    public List<DesserteResponseDTO> ajouterArret(Long ligneId, DesserteRequestDTO dto) {
        LigneEntity ligne = trouverOu404(ligneRepository, ligneId, "Ligne");
        ArretEntity arret = trouverOu404(arretRepository, dto.getArretId(), "Arrêt");
        List<DesserteEntity> trace = desserteRepository.findByLigneIdAndSensOrderByPositionAsc(ligneId, dto.getSens());

        for (DesserteEntity existante : trace) {
            if (existante.getArret().getId().equals(arret.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "L'arrêt " + arret.getNom() + " est déjà présent sur la ligne " + ligne.getNumero()
                                + " (" + dto.getSens() + ") à la position " + existante.getPosition());
            }
        }
        if (dto.getPosition() > trace.size() + 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Position " + dto.getPosition() + " impossible : la ligne compte " + trace.size() + " arrêt(s) dans ce sens");
        }

        // F11 - décalage : chaque arrêt à partir de la position demandée recule d'un rang, en partant du dernier et en
        // écrivant chaque ligne aussitôt, pour ne jamais avoir deux arrêts à la même position (contrainte d'unicité)
        for (int i = trace.size() - 1; i >= 0; i--) {
            DesserteEntity existante = trace.get(i);
            if (existante.getPosition() >= dto.getPosition()) {
                existante.setPosition(existante.getPosition() + 1);
                desserteRepository.saveAndFlush(existante);
            }
        }
        DesserteEntity nouvelle = new DesserteEntity();
        nouvelle.setLigne(ligne);
        nouvelle.setArret(arret);
        nouvelle.setSens(dto.getSens());
        nouvelle.setPosition(dto.getPosition());
        desserteRepository.save(nouvelle);
        return arretsDeLaLigne(ligneId);
    }

    // Entity -> DTO
    private DesserteResponseDTO entityVersResponseDTO(DesserteEntity desserte) {
        DesserteResponseDTO dto = new DesserteResponseDTO();
        dto.setLigneId(desserte.getLigne().getId());
        dto.setSens(desserte.getSens());
        dto.setPosition(desserte.getPosition());
        dto.setArretId(desserte.getArret().getId());
        dto.setArretNom(desserte.getArret().getNom());
        dto.setCommune(desserte.getArret().getCommune());
        dto.setLatitude(desserte.getArret().getLatitude());
        dto.setLongitude(desserte.getArret().getLongitude());
        return dto;
    }
}

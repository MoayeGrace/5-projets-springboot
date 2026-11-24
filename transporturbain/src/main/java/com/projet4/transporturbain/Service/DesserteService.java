package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.DesserteResponseDTO;
import com.projet4.transporturbain.Entity.DesserteEntity;
import com.projet4.transporturbain.Repository.DesserteRepository;
import com.projet4.transporturbain.Repository.LigneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

// F6 / F11 - tracé des lignes : quels arrêts, dans quel ordre
@Service
@RequiredArgsConstructor
public class DesserteService {

    private final DesserteRepository desserteRepository;
    private final LigneRepository ligneRepository;

    // F6 - GET /lignes/{id}/arrets : arrêts dans l'ordre de desserte, sens par sens (vide si aucun, 404 si ligne inconnue)
    public List<DesserteResponseDTO> arretsDeLaLigne(Long ligneId) {
        trouverOu404(ligneRepository, ligneId, "Ligne");
        return desserteRepository.findByLigneIdOrderBySensAscPositionAsc(ligneId)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
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

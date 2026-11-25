package com.projet5.lapaie.Service;

import com.projet5.lapaie.DTO.MasseSalarialeDTO;
import com.projet5.lapaie.Entity.BulletinEntity;
import com.projet5.lapaie.Repository.BulletinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final BulletinRepository bulletinRepository;

    // F17 - GET /bulletins/masse-salariale?mois= : somme des bruts, retenues et nets du mois (en cours par défaut),
    // par service et au total ; les bulletins non édités sont inclus mais signalés comme provisoires
    public MasseSalarialeDTO masseSalariale(String mois) {
        String periode = (mois != null) ? verifierFormat(mois) : YearMonth.now().toString();
        MasseSalarialeDTO dto = new MasseSalarialeDTO();
        dto.setMois(periode);

        Map<String, MasseSalarialeDTO.Totaux> parService = new TreeMap<>();
        for (BulletinEntity bulletin : bulletinRepository.findByMois(periode)) {
            String service = bulletin.getEmploye().getService();
            MasseSalarialeDTO.Totaux totaux = parService.computeIfAbsent(service, s -> {
                MasseSalarialeDTO.Totaux t = new MasseSalarialeDTO.Totaux();
                t.setService(s);
                return t;
            });
            ajouter(totaux, bulletin);
            ajouter(dto.getTotal(), bulletin);
        }
        dto.getServices().addAll(parService.values());
        return dto;
    }

    // F17 - cumule un bulletin dans des totaux
    private void ajouter(MasseSalarialeDTO.Totaux totaux, BulletinEntity bulletin) {
        totaux.setBrut(totaux.getBrut().add(bulletin.getBrut()));
        totaux.setRetenues(totaux.getRetenues().add(bulletin.getRetenues()));
        totaux.setNet(totaux.getNet().add(bulletin.getNet()));
        totaux.setEffectif(totaux.getEffectif() + 1);
        if (!bulletin.getEdite()) {
            totaux.setBulletinsProvisoires(totaux.getBulletinsProvisoires() + 1);
            totaux.setProvisoire(true);
        }
    }

    // Vérifie le format AAAA-MM (400 sinon)
    private String verifierFormat(String mois) {
        if (!mois.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mois doit être au format AAAA-MM");
        }
        return mois;
    }
}

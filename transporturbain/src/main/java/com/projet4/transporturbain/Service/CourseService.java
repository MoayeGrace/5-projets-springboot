package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.CourseRequestDTO;
import com.projet4.transporturbain.DTO.CourseResponseDTO;
import com.projet4.transporturbain.DTO.PageResponseDTO;
import com.projet4.transporturbain.DTO.ServiceJourDTO;
import com.projet4.transporturbain.DTO.StatutRequestDTO;
import com.projet4.transporturbain.Entity.BusEntity;
import com.projet4.transporturbain.Entity.ConducteurEntity;
import com.projet4.transporturbain.Entity.CourseEntity;
import com.projet4.transporturbain.Entity.LigneEntity;
import com.projet4.transporturbain.Entity.StatutCourse;
import com.projet4.transporturbain.Repository.BusRepository;
import com.projet4.transporturbain.Repository.ConducteurRepository;
import com.projet4.transporturbain.Repository.CourseRepository;
import com.projet4.transporturbain.Repository.LigneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.projet4.transporturbain.Service.ServiceUtils.trouverOu404;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LigneRepository ligneRepository;
    private final BusRepository busRepository;
    private final ConducteurRepository conducteurRepository;

    // F10 - taille de page maximale autorisée
    private static final int TAILLE_PAGE_MAX = 200;

    // F10 - GET /courses?page=&size=&sort= : courses page par page (une taille > 200 est ramenée à 200)
    public PageResponseDTO<CourseResponseDTO> lister(Pageable pageable) {
        if (pageable.getPageSize() > TAILLE_PAGE_MAX) {
            pageable = PageRequest.of(pageable.getPageNumber(), TAILLE_PAGE_MAX, pageable.getSort());
        }
        return new PageResponseDTO<>(courseRepository.findAll(pageable).map(this::entityVersResponseDTO));
    }

    // F7 - GET /lignes/{id}/courses?date= : courses de la ligne ce jour-là (aujourd'hui par défaut), par heure de départ
    public List<CourseResponseDTO> listerParLigne(Long ligneId, LocalDate date) {
        trouverOu404(ligneRepository, ligneId, "Ligne");
        LocalDate jour = (date != null) ? date : LocalDate.now();
        return courseRepository.findByLigneIdAndDateCourseOrderByHeureDepartAsc(ligneId, jour)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F8 - GET /conducteurs/{id}/courses?du=&au= : programme du conducteur (semaine en cours par défaut),
    // trié par date puis heure ; 400 si la période dépasse trois mois ou si "du" est après "au"
    public List<CourseResponseDTO> listerParConducteur(Long conducteurId, LocalDate du, LocalDate au) {
        trouverOu404(conducteurRepository, conducteurId, "Conducteur");
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate debut = (du != null) ? du : lundi;
        LocalDate fin = (au != null) ? au : lundi.plusDays(6);
        if (debut.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début doit précéder la date de fin");
        }
        if (fin.isAfter(debut.plusMonths(3))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La période demandée ne peut pas dépasser trois mois");
        }
        return courseRepository.findByConducteurIdAndDateCourseBetweenOrderByDateCourseAscHeureDepartAsc(conducteurId, debut, fin)
                .stream()
                .map(this::entityVersResponseDTO)
                .toList();
    }

    // F17 - GET /courses/jour?date= : courses de la journée (aujourd'hui par défaut) groupées par ligne puis par heure,
    // avec le nombre de courses annulées par ligne
    public ServiceJourDTO serviceDuJour(LocalDate date) {
        LocalDate jour = (date != null) ? date : LocalDate.now();
        ServiceJourDTO dto = new ServiceJourDTO();
        dto.setDate(jour);
        Map<Long, ServiceJourDTO.Ligne> parLigne = new LinkedHashMap<>();
        for (CourseEntity course : courseRepository.findByDateCourseOrderByLigneNumeroAscHeureDepartAsc(jour)) {
            ServiceJourDTO.Ligne ligne = parLigne.computeIfAbsent(course.getLigne().getId(), id -> {
                ServiceJourDTO.Ligne nouvelle = new ServiceJourDTO.Ligne();
                nouvelle.setLigneId(id);
                nouvelle.setLigneNumero(course.getLigne().getNumero());
                nouvelle.setLigneLibelle(course.getLigne().getLibelle());
                return nouvelle;
            });
            ligne.getCourses().add(entityVersResponseDTO(course));
            ligne.setNombreCourses(ligne.getNombreCourses() + 1);
            if (course.getStatut() == StatutCourse.ANNULEE) {
                ligne.setNombreAnnulees(ligne.getNombreAnnulees() + 1);
            }
        }
        dto.getLignes().addAll(parLigne.values());
        return dto;
    }

    // GET /courses/{id} : récupère une course par son id (404 si introuvable)
    public CourseResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(courseRepository, id, "Course"));
    }

    // POST /courses : enregistre une nouvelle course (INSERT)
    public CourseResponseDTO creer(CourseRequestDTO dto) {
        CourseEntity course = new CourseEntity();
        dtoVersEntity(dto, course);
        verifierPermis(course);            // F14
        verifierBusLibre(course);          // F12
        verifierConducteurLibre(course);   // F13
        return entityVersResponseDTO(courseRepository.save(course));
    }

    // PUT /courses/{id} : seule une course PREVUE se modifie (404 si inconnue, 409 sinon) ; ses contrôles sont refaits
    public CourseResponseDTO modifier(Long id, CourseRequestDTO dto) {
        CourseEntity course = trouverOu404(courseRepository, id, "Course");
        if (course.getStatut() != StatutCourse.PREVUE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une course PREVUE peut être modifiée (statut actuel : " + course.getStatut() + ")");
        }
        dtoVersEntity(dto, course);
        verifierPermis(course);            // F14 - changement de conducteur ou de date
        verifierBusLibre(course);          // F12 - nouveau bus ou nouvel horaire : le créneau est revérifié
        verifierConducteurLibre(course);   // F13 - nouveau conducteur ou nouvel horaire : idem
        return entityVersResponseDTO(courseRepository.save(course));
    }

    // F14 - le permis du conducteur doit être valide après la date de la course (400 indiquant la date d'expiration)
    private void verifierPermis(CourseEntity course) {
        LocalDate validite = course.getConducteur().getDateValiditePermis();
        if (validite == null || !validite.isAfter(course.getDateCourse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le permis de " + course.getConducteur().getNom() + " expire le " + validite
                            + " : il ne peut pas assurer une course le " + course.getDateCourse());
        }
    }

    // F13 - aucune autre course non annulée du conducteur ne doit recouvrir le créneau départ -> arrivée (409) ;
    // vérification indépendante de celle du bus
    private void verifierConducteurLibre(CourseEntity course) {
        if (course.getStatut() == StatutCourse.ANNULEE) {
            return;
        }
        for (CourseEntity autre : courseRepository.findByConducteurIdAndStatutNot(course.getConducteur().getId(), StatutCourse.ANNULEE)) {
            boolean chevauche = autre.depart().isBefore(course.arriveePrevue())
                    && autre.arriveePrevue().isAfter(course.depart());
            if (!autre.getId().equals(course.getId()) && chevauche) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le conducteur " + course.getConducteur().getNom() + " assure déjà la course " + autre.getId()
                                + " de " + autre.depart() + " à " + autre.arriveePrevue());
            }
        }
    }

    // F12 - aucune autre course non annulée du bus ne doit recouvrir le créneau, temps de retour au terminus inclus (409)
    private void verifierBusLibre(CourseEntity course) {
        if (course.getStatut() == StatutCourse.ANNULEE) {
            return;
        }
        for (CourseEntity autre : courseRepository.findByBusIdAndStatutNot(course.getBus().getId(), StatutCourse.ANNULEE)) {
            boolean chevauche = autre.depart().isBefore(course.finOccupationBus())
                    && autre.finOccupationBus().isAfter(course.depart());
            if (!autre.getId().equals(course.getId()) && chevauche) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le bus " + course.getBus().getImmatriculation() + " est déjà pris par la course " + autre.getId()
                                + " de " + autre.depart() + " à " + autre.finOccupationBus() + " (retour au terminus inclus)");
            }
        }
    }

    // DELETE /courses/{id} : supprime une course PREVUE ou ANNULEE (404 si inconnue) ; une course partie ou terminée
    // reste pour l'historique et les statistiques (409)
    public void supprimer(Long id) {
        CourseEntity course = trouverOu404(courseRepository, id, "Course");
        if (course.getStatut() == StatutCourse.EN_COURS || course.getStatut() == StatutCourse.TERMINEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une course " + course.getStatut() + " ne se supprime pas : elle sert à l'historique et aux statistiques");
        }
        courseRepository.delete(course);
    }

    // F15 - POST /courses/{id}/statut : fait évoluer le statut selon les transitions autorisées (409 sinon) ;
    // au passage à TERMINEE, l'heure réelle d'arrivée est enregistrée (celle fournie, sinon l'heure actuelle)
    public CourseResponseDTO changerStatut(Long id, StatutRequestDTO dto) {
        CourseEntity course = trouverOu404(courseRepository, id, "Course");
        StatutCourse nouveau = dto.getStatut();
        if (!course.getStatut().statutsAtteignables().contains(nouveau)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Passage de " + course.getStatut() + " à " + nouveau + " interdit. Statuts atteignables : "
                            + course.getStatut().statutsAtteignables());
        }
        if (nouveau == StatutCourse.TERMINEE) {
            course.setHeureArriveeReelle(dto.getHeureArriveeReelle() != null ? dto.getHeureArriveeReelle() : LocalTime.now().withNano(0));
        }
        course.setStatut(nouveau);
        return entityVersResponseDTO(courseRepository.save(course));
    }

    // DTO -> Entity (ids transformés en objets, 404 s'ils n'existent pas ; le statut ne change pas ici, F15) ;
    // F12 - arrivée prévue = départ + durée du trajet, la course doit finir le jour même (400 sinon)
    private void dtoVersEntity(CourseRequestDTO dto, CourseEntity course) {
        LigneEntity ligne = trouverOu404(ligneRepository, dto.getLigneId(), "Ligne");
        BusEntity bus = trouverOu404(busRepository, dto.getBusId(), "Bus");
        ConducteurEntity conducteur = trouverOu404(conducteurRepository, dto.getConducteurId(), "Conducteur");
        LocalDateTime arrivee = dto.getDateCourse().atTime(dto.getHeureDepart()).plusMinutes(ligne.getDureeTrajetMinutes());
        if (!arrivee.toLocalDate().equals(dto.getDateCourse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La course doit arriver au terminus le jour même (arrivée prévue : " + arrivee + ")");
        }

        course.setDateCourse(dto.getDateCourse());
        course.setHeureDepart(dto.getHeureDepart());
        course.setHeureArriveePrevue(arrivee.toLocalTime());
        course.setLigne(ligne);
        course.setBus(bus);
        course.setConducteur(conducteur);
    }

    // Entity -> DTO
    private CourseResponseDTO entityVersResponseDTO(CourseEntity course) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setId(course.getId());
        dto.setDateCourse(course.getDateCourse());
        dto.setHeureDepart(course.getHeureDepart());
        dto.setLigneId(course.getLigne().getId());
        dto.setLigneNumero(course.getLigne().getNumero());
        dto.setBusId(course.getBus().getId());
        dto.setBusImmatriculation(course.getBus().getImmatriculation());
        dto.setConducteurId(course.getConducteur().getId());
        dto.setConducteurNom(course.getConducteur().getNom());
        dto.setStatut(course.getStatut());
        dto.setHeureArriveePrevue(course.arriveePrevue().toLocalTime());   // F15
        dto.setHeureArriveeReelle(course.getHeureArriveeReelle());         // F15
        return dto;
    }
}
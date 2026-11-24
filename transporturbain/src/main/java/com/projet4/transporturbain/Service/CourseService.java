package com.projet4.transporturbain.Service;

import com.projet4.transporturbain.DTO.CourseRequestDTO;
import com.projet4.transporturbain.DTO.CourseResponseDTO;
import com.projet4.transporturbain.DTO.PageResponseDTO;
import com.projet4.transporturbain.Entity.BusEntity;
import com.projet4.transporturbain.Entity.ConducteurEntity;
import com.projet4.transporturbain.Entity.CourseEntity;
import com.projet4.transporturbain.Entity.LigneEntity;
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
import java.util.List;

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

    // GET /courses/{id} : récupère une course par son id (404 si introuvable)
    public CourseResponseDTO detail(Long id) {
        return entityVersResponseDTO(trouverOu404(courseRepository, id, "Course"));
    }

    // POST /courses : enregistre une nouvelle course (INSERT)
    public CourseResponseDTO creer(CourseRequestDTO dto) {
        CourseEntity course = new CourseEntity();
        dtoVersEntity(dto, course);
        return entityVersResponseDTO(courseRepository.save(course));
    }

    // PUT /courses/{id} : charge l'élément existant (404 sinon), remplace ses champs, puis UPDATE
    public CourseResponseDTO modifier(Long id, CourseRequestDTO dto) {
        CourseEntity course = trouverOu404(courseRepository, id, "Course");
        dtoVersEntity(dto, course);
        return entityVersResponseDTO(courseRepository.save(course));
    }

    // DELETE /courses/{id} : vérifie que l'élément existe (404 sinon), puis le supprime
    public void supprimer(Long id) {
        courseRepository.delete(trouverOu404(courseRepository, id, "Course"));
    }

    // DTO -> Entity (ids transformés en objets, 404 s'ils n'existent pas ; statut absent = inchangé)
    private void dtoVersEntity(CourseRequestDTO dto, CourseEntity course) {
        LigneEntity ligne = trouverOu404(ligneRepository, dto.getLigneId(), "Ligne");
        BusEntity bus = trouverOu404(busRepository, dto.getBusId(), "Bus");
        ConducteurEntity conducteur = trouverOu404(conducteurRepository, dto.getConducteurId(), "Conducteur");

        course.setDateCourse(dto.getDateCourse());
        course.setHeureDepart(dto.getHeureDepart());
        course.setLigne(ligne);
        course.setBus(bus);
        course.setConducteur(conducteur);
        if (dto.getStatut() != null) {
            course.setStatut(dto.getStatut());
        }
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
        return dto;
    }
}
package com.example.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.Tutorial;
import com.example.exception.BadRequestException;
import com.example.services.TutorialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;

    /**
     * Ejemplos de peticiones:
     *
     * GET /api/tutorials                         -> todos, ordenados por titulo
     * GET /api/tutorials?title=spring            -> filtrados por titulo, ordenados
     * GET /api/tutorials?page=0&size=3           -> paginados (y ordenados por titulo)
     * GET /api/tutorials?title=spring&page=0&size=3
     *
     * page y size son opcionales; solo se pagina si llegan los dos.
     */
    @GetMapping("/tutorials")
    public ResponseEntity<Map<String, Object>> getAllTutorials(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Map<String, Object> responseAsMap = new HashMap<>();
        Sort sort = Sort.by("title");

        if (page != null && size != null) {

            if (page < 0 || size < 1) {
                throw new BadRequestException(
                        "page debe ser mayor o igual que 0 y size mayor o igual que 1");
            }

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Tutorial> tutorialPage = (title == null)
                    ? tutorialService.findAll(pageable)
                    : tutorialService.findByTitleContaining(title, pageable);

            responseAsMap.put("tutorials", tutorialPage.getContent());
            responseAsMap.put("currentPage", tutorialPage.getNumber());
            responseAsMap.put("totalItems", tutorialPage.getTotalElements());
            responseAsMap.put("totalPages", tutorialPage.getTotalPages());

        } else {

            List<Tutorial> tutorials = (title == null)
                    ? tutorialService.findAll(sort)
                    : tutorialService.findByTitleContaining(title, sort);

            responseAsMap.put("tutorials", tutorials);
        }

        return new ResponseEntity<>(responseAsMap, HttpStatus.OK);
    }

    @GetMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
        return new ResponseEntity<>(tutorialService.findById(id), HttpStatus.OK);
    }

    @PostMapping("/tutorials")
    public ResponseEntity<Tutorial> createTutorial(@Valid @RequestBody Tutorial tutorial) {
        Tutorial created = tutorialService.save(
                Tutorial.builder()
                        .title(tutorial.getTitle())
                        .description(tutorial.getDescription())
                        .published(true)
                        .build());

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id,
            @Valid @RequestBody Tutorial tutorial) {
        return new ResponseEntity<>(tutorialService.update(id, tutorial), HttpStatus.OK);
    }

    @DeleteMapping("/tutorials/{id}")
    public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
        tutorialService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/tutorials")
    public ResponseEntity<HttpStatus> deleteAllTutorials() {
        tutorialService.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tutorials/published")
    public ResponseEntity<List<Tutorial>> findByPublished() {
        List<Tutorial> tutorials = tutorialService.findByPublished(true);

        if (tutorials.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

}

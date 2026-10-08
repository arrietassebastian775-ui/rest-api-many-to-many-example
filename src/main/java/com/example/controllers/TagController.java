package com.example.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.services.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.findAll();

        if (tags.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/tags/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable("id") long id) {
        return new ResponseEntity<>(tagService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/tags/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable("id") long id,
            @Valid @RequestBody Tag tagRequest) {
        return new ResponseEntity<>(tagService.update(id, tagRequest), HttpStatus.OK);
    }

    // Antes estaba por error en TutorialController; la URL no cambia
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<HttpStatus> deleteTag(@PathVariable("id") long id) {
        tagService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tags/{tagId}/tutorials")
    public ResponseEntity<List<Tutorial>> getAllTutorialsByTagId(@PathVariable("tagId") long tagId) {
        return new ResponseEntity<>(tagService.findTutorialsByTagId(tagId), HttpStatus.OK);
    }

    @GetMapping("/tutorials/{tutorialId}/tags")
    public ResponseEntity<List<Tag>> getAllTagsByTutorialId(@PathVariable("tutorialId") long tutorialId) {
        return new ResponseEntity<>(tagService.findByTutorialId(tutorialId), HttpStatus.OK);
    }

    /**
     * El cuerpo puede ser {"id": 3} para asociar una etiqueta existente, o
     * {"name": "java"} para crear una nueva. Por eso NO lleva @Valid: la validacion
     * del nombre se hace en el servicio, solo cuando hay que crear la etiqueta.
     */
    @PostMapping("/tutorials/{tutorialId}/tags")
    public ResponseEntity<Tag> addTag(@PathVariable("tutorialId") long tutorialId,
            @RequestBody Tag tagRequest) {
        return new ResponseEntity<>(tagService.addTagToTutorial(tutorialId, tagRequest), HttpStatus.CREATED);
    }

    // Nuevo: quita la etiqueta de un tutorial sin borrar la etiqueta
    @DeleteMapping("/tutorials/{tutorialId}/tags/{tagId}")
    public ResponseEntity<HttpStatus> removeTagFromTutorial(@PathVariable("tutorialId") long tutorialId,
            @PathVariable("tagId") long tagId) {
        tagService.removeTagFromTutorial(tutorialId, tagId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

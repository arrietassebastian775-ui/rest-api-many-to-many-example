package com.example.services;

import java.util.List;

import com.example.entities.Tag;
import com.example.entities.Tutorial;

public interface TagService {

    List<Tag> findAll();

    // Lanza ResourceNotFoundException si la etiqueta no existe
    Tag findById(long id);

    Tag save(Tag tag);

    // Lanza ResourceNotFoundException si la etiqueta no existe
    Tag update(long id, Tag tagRequest);

    // Desvincula la etiqueta de sus tutoriales y la elimina
    void deleteById(long id);

    // Etiquetas de un tutorial. Lanza ResourceNotFoundException si el tutorial no existe
    List<Tag> findByTutorialId(long tutorialId);

    // Tutoriales de una etiqueta. Lanza ResourceNotFoundException si la etiqueta no existe
    List<Tutorial> findTutorialsByTagId(long tagId);

    /**
     * Si tagRequest trae un id distinto de cero se asocia la etiqueta existente; si
     * no, se crea una etiqueta nueva (el nombre es obligatorio) y se asocia.
     */
    Tag addTagToTutorial(long tutorialId, Tag tagRequest);

    // Quita la etiqueta del tutorial (no borra la etiqueta)
    void removeTagFromTutorial(long tutorialId, long tagId);
}

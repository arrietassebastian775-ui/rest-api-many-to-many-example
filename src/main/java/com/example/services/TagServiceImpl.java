package com.example.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dao.TagDao;
import com.example.dao.TutorialDao;
import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagDao tagDao;
    private final TutorialDao tutorialDao;

    @Override
    public List<Tag> findAll() {
        return tagDao.findAll();
    }

    @Override
    public Tag findById(long id) {
        return tagDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se ha encontrado la etiqueta con id = " + id));
    }

    @Override
    @Transactional
    public Tag save(Tag tag) {
        return tagDao.save(tag);
    }

    @Override
    @Transactional
    public Tag update(long id, Tag tagRequest) {
        Tag tag = findById(id);
        tag.setName(tagRequest.getName());
        return tagDao.save(tag);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        Tag tag = findById(id);

        /*
         * Tag es el lado inverso de la relacion (mappedBy), el dueño es Tutorial. Si
         * borramos la etiqueta sin mas, la tabla intermedia sigue teniendo filas que la
         * referencian y MySQL rechaza el DELETE por la clave foranea. Por eso primero la
         * quitamos de cada tutorial (eso si borra las filas de la tabla intermedia).
         */
        new ArrayList<>(tag.getTutorials())
                .forEach(tutorial -> tutorial.getTags().remove(tag));

        tagDao.delete(tag);
    }

    @Override
    public List<Tag> findByTutorialId(long tutorialId) {
        if (!tutorialDao.existsById(tutorialId)) {
            throw new ResourceNotFoundException(
                    "No se ha encontrado el tutorial con id = " + tutorialId);
        }
        return tagDao.findTagsByTutorialsId(tutorialId);
    }

    @Override
    public List<Tutorial> findTutorialsByTagId(long tagId) {
        if (!tagDao.existsById(tagId)) {
            throw new ResourceNotFoundException(
                    "No se ha encontrado la etiqueta con id = " + tagId);
        }
        return tutorialDao.findTutorialsByTagsId(tagId);
    }

    @Override
    @Transactional
    public Tag addTagToTutorial(long tutorialId, Tag tagRequest) {
        Tutorial tutorial = tutorialDao.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se ha encontrado el tutorial con id = " + tutorialId));

        // Caso 1: la etiqueta ya existe, solo hay que asociarla
        if (tagRequest.getId() != 0L) {
            Tag existente = tagDao.findById(tagRequest.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se ha encontrado la etiqueta con id = " + tagRequest.getId()));

            tutorial.addTag(existente);
            tutorialDao.save(tutorial);
            return existente;
        }

        /*
         * Caso 2: etiqueta nueva. Aqui no podemos usar @Valid en el controlador porque
         * en el caso 1 el cuerpo puede ser solo {"id": 3}, sin nombre. Validamos el
         * nombre a mano solo cuando hay que crear la etiqueta.
         */
        if (tagRequest.getName() == null || tagRequest.getName().isBlank()) {
            throw new BadRequestException("El nombre de la etiqueta es obligatorio");
        }

        tutorial.addTag(tagRequest);
        return tagDao.save(tagRequest);
    }

    @Override
    @Transactional
    public void removeTagFromTutorial(long tutorialId, long tagId) {
        Tutorial tutorial = tutorialDao.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se ha encontrado el tutorial con id = " + tutorialId));

        boolean asociada = tutorial.getTags().stream().anyMatch(tag -> tag.getId() == tagId);
        if (!asociada) {
            throw new ResourceNotFoundException("El tutorial con id = " + tutorialId
                    + " no tiene la etiqueta con id = " + tagId);
        }

        tutorial.removeTag(tagId);
        tutorialDao.save(tutorial);
    }

}

package com.example.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dao.TutorialDao;
import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TutorialServiceImpl implements TutorialService {

    private final TutorialDao tutorialDao;

    @Override
    public Page<Tutorial> findAll(Pageable pageable) {
        return tutorialDao.findAll(pageable);
    }

    @Override
    public List<Tutorial> findAll(Sort sort) {
        return tutorialDao.findAll(sort);
    }

    @Override
    public Page<Tutorial> findByTitleContaining(String title, Pageable pageable) {
        return tutorialDao.findByTitleContaining(title, pageable);
    }

    @Override
    public List<Tutorial> findByTitleContaining(String title, Sort sort) {
        return tutorialDao.findByTitleContaining(title, sort);
    }

    @Override
    public List<Tutorial> findByPublished(boolean published) {
        return tutorialDao.findByPublished(published);
    }

    @Override
    public Tutorial findById(long id) {
        return tutorialDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se ha encontrado el tutorial con id = " + id));
    }

    @Override
    @Transactional
    public Tutorial save(Tutorial tutorial) {
        return tutorialDao.save(tutorial);
    }

    @Override
    @Transactional
    public Tutorial update(long id, Tutorial tutorial) {
        Tutorial existente = findById(id);

        existente.setTitle(tutorial.getTitle());
        existente.setDescription(tutorial.getDescription());
        existente.setPublished(tutorial.getPublished());

        return tutorialDao.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        // deleteById de Spring Data no falla si el id no existe, asi que lo
        // comprobamos nosotros para poder responder con un 404
        if (!tutorialDao.existsById(id)) {
            throw new ResourceNotFoundException("No se ha encontrado el tutorial con id = " + id);
        }
        tutorialDao.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAll() {
        tutorialDao.deleteAll();
    }

}

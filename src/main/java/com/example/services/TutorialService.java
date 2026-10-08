package com.example.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.entities.Tutorial;

public interface TutorialService {

    Page<Tutorial> findAll(Pageable pageable);

    List<Tutorial> findAll(Sort sort);

    Page<Tutorial> findByTitleContaining(String title, Pageable pageable);

    List<Tutorial> findByTitleContaining(String title, Sort sort);

    List<Tutorial> findByPublished(boolean published);

    // Lanza ResourceNotFoundException si el tutorial no existe
    Tutorial findById(long id);

    Tutorial save(Tutorial tutorial);

    // Lanza ResourceNotFoundException si el tutorial no existe
    Tutorial update(long id, Tutorial tutorial);

    // Lanza ResourceNotFoundException si el tutorial no existe
    void deleteById(long id);

    void deleteAll();
}
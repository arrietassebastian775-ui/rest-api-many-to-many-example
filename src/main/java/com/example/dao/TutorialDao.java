package com.example.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Tutorial;

public interface TutorialDao extends JpaRepository<Tutorial, Long> {

    List<Tutorial> findByPublished(Boolean published);

    // Busqueda por titulo, ordenada (sin paginacion)
    List<Tutorial> findByTitleContaining(String title, Sort sort);

    // Busqueda por titulo, paginada (el orden viaja dentro del Pageable)
    Page<Tutorial> findByTitleContaining(String title, Pageable pageable);

    List<Tutorial> findTutorialsByTagsId(Long tagId);

}

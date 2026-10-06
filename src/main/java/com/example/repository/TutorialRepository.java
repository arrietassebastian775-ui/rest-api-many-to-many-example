package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Tutorial;
import java.util.List;


public interface TutorialRepository extends JpaRepository<Tutorial, Long>{

    List<Tutorial> findByPublished(Boolean published);

    List<Tutorial> findByTitleContaining(String title);

    List<Tutorial> findTutorialsByTagsId(Long tagId);

}

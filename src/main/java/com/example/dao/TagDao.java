package com.example.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Tag;

public interface TagDao extends JpaRepository<Tag, Long> {

    List<Tag> findTagsByTutorialsId(Long tutorialsId);

}

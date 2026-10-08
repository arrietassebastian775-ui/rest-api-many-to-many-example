package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.services.TagService;
import com.example.services.TutorialService;

/**
 * Carga datos de ejemplo al arrancar la aplicacion (con ddl-auto=create-drop las
 * tablas se crean vacias en cada arranque).
 */
@Configuration
public class CreatesSamplesData {

    @Bean
    public CommandLineRunner samplesData(TutorialService tutorialService, TagService tagService) {

        return args -> {

            // Etiquetas
            Tag etiquetaJava = tagService.save(Tag.builder().name("java").build());
            Tag etiquetaSpring = tagService.save(Tag.builder().name("spring").build());
            Tag etiquetaBd = tagService.save(Tag.builder().name("bases de datos").build());
            Tag etiquetaTesting = tagService.save(Tag.builder().name("testing").build());

            // Tutoriales
            Tutorial introSpring = tutorialService.save(Tutorial.builder()
                    .title("Introduccion a Spring Boot")
                    .description("Primer proyecto con Spring Boot")
                    .published(true)
                    .build());

            Tutorial jpaManyToMany = tutorialService.save(Tutorial.builder()
                    .title("JPA y relaciones many to many")
                    .description("Como mapear una relacion muchos a muchos")
                    .published(true)
                    .build());

            Tutorial testsSpring = tutorialService.save(Tutorial.builder()
                    .title("Tests con JUnit y Mockito")
                    .description("Tests de DAO, servicios y controladores")
                    .published(false)
                    .build());

            // Relaciones: addTagToTutorial recibe una etiqueta ya persistida (id != 0),
            // asi que solo la asocia al tutorial
            tagService.addTagToTutorial(introSpring.getId(), etiquetaJava);
            tagService.addTagToTutorial(introSpring.getId(), etiquetaSpring);

            tagService.addTagToTutorial(jpaManyToMany.getId(), etiquetaJava);
            tagService.addTagToTutorial(jpaManyToMany.getId(), etiquetaSpring);
            tagService.addTagToTutorial(jpaManyToMany.getId(), etiquetaBd);

            tagService.addTagToTutorial(testsSpring.getId(), etiquetaJava);
            tagService.addTagToTutorial(testsSpring.getId(), etiquetaTesting);
        };
    }
}
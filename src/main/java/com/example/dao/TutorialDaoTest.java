package com.example.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Sort;

import com.example.entities.Tag;
import com.example.entities.Tutorial;

/* Solo prueba las entidades y los DAO, sin levantar todo el contexto de Spring. Cada
 * test es transaccional por defecto y se hace roll-back al terminar. Se usa la base de
 * datos real (MySQL), igual que en el proyecto de clase, asi que tiene que estar
 * arrancada. Por eso las comprobaciones no dependen del numero total de filas. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class TutorialDaoTest {

	@Autowired
	private TutorialDao tutorialDao;

	@Autowired
	private TagDao tagDao;

	private Tutorial tutorialPublicado;
	private Tutorial tutorialBorrador;

	@BeforeEach
	void setUp() {

		tutorialPublicado = Tutorial.builder()
				.title("Zzz Tutorial publicado de prueba")
				.description("Descripcion del tutorial publicado")
				.published(true)
				.build();

		tutorialBorrador = Tutorial.builder()
				.title("Zzz Tutorial borrador de prueba")
				.description("Descripcion del tutorial en borrador")
				.published(false)
				.build();
	}

	@Test
	@DisplayName("Test para persistir un tutorial")
	void testSaveTutorial() {

		// when
		Tutorial guardado = tutorialDao.save(tutorialPublicado);

		// then
		assertThat(guardado).isNotNull();
		assertThat(guardado.getId()).isGreaterThan(0);
	}

	@Test
	@DisplayName("Test para recuperar solo los tutoriales publicados")
	void testFindByPublished() {

		// given
		tutorialDao.save(tutorialPublicado);
		tutorialDao.save(tutorialBorrador);

		// when
		List<Tutorial> publicados = tutorialDao.findByPublished(true);

		// then
		assertThat(publicados)
				.allMatch(Tutorial::getPublished)
				.extracting(Tutorial::getTitle)
				.contains(tutorialPublicado.getTitle())
				.doesNotContain(tutorialBorrador.getTitle());
	}

	@Test
	@DisplayName("Test para buscar tutoriales por parte del titulo")
	void testFindByTitleContaining() {

		// given
		tutorialDao.save(tutorialPublicado);
		tutorialDao.save(tutorialBorrador);

		// when
		List<Tutorial> encontrados = tutorialDao.findByTitleContaining(
				"Zzz Tutorial publicado", Sort.by("title"));

		// then
		assertThat(encontrados).hasSize(1);
		assertThat(encontrados.get(0).getTitle()).isEqualTo(tutorialPublicado.getTitle());
	}

	@Test
	@DisplayName("Test de la relacion many to many: etiquetas de un tutorial y tutoriales de una etiqueta")
	void testManyToMany() {

		// given
		Tag etiqueta = tagDao.save(Tag.builder().name("zzz-etiqueta").build());
		tutorialPublicado.addTag(etiqueta);
		Tutorial guardado = tutorialDao.save(tutorialPublicado);

		// when
		List<Tag> etiquetasDelTutorial = tagDao.findTagsByTutorialsId(guardado.getId());
		List<Tutorial> tutorialesDeLaEtiqueta = tutorialDao.findTutorialsByTagsId(etiqueta.getId());

		// then
		assertThat(etiquetasDelTutorial).hasSize(1);
		assertThat(etiquetasDelTutorial.get(0).getName()).isEqualTo("zzz-etiqueta");
		assertThat(tutorialesDeLaEtiqueta).hasSize(1);
		assertThat(tutorialesDeLaEtiqueta.get(0).getId()).isEqualTo(guardado.getId());
	}

}
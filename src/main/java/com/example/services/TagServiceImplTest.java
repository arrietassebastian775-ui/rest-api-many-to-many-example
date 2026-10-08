package com.example.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dao.TagDao;
import com.example.dao.TutorialDao;
import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

	@Mock
	private TagDao tagDao;

	@Mock
	private TutorialDao tutorialDao;

	@InjectMocks
	private TagServiceImpl tagService;

	private Tutorial tutorial;
	private Tag tagExistente;

	@BeforeEach
	void setUp() {

		tutorial = Tutorial.builder()
				.id(1L)
				.title("Spring Boot")
				.description("Tutorial de Spring Boot")
				.published(true)
				.build();

		tagExistente = Tag.builder()
				.id(5L)
				.name("spring")
				.build();
	}

	@Test
	@DisplayName("Test del servicio para recuperar una etiqueta que no existe")
	void testFindByIdNotFound() {

		given(tagDao.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> tagService.findById(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	@DisplayName("Test del servicio para actualizar una etiqueta")
	void testUpdate() {

		given(tagDao.findById(5L)).willReturn(Optional.of(tagExistente));
		given(tagDao.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

		Tag actualizada = tagService.update(5L, Tag.builder().name("spring-boot").build());

		assertThat(actualizada.getId()).isEqualTo(5L);
		assertThat(actualizada.getName()).isEqualTo("spring-boot");
	}

	@Test
	@DisplayName("Test del servicio para crear una etiqueta nueva y asociarla a un tutorial")
	void testAddNewTagToTutorial() {

		Tag nueva = Tag.builder().name("java").build();

		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial));
		given(tagDao.save(nueva)).willReturn(nueva);

		Tag resultado = tagService.addTagToTutorial(1L, nueva);

		assertThat(resultado.getName()).isEqualTo("java");
		assertThat(tutorial.getTags()).contains(nueva);
	}

	@Test
	@DisplayName("Test del servicio para asociar una etiqueta existente a un tutorial")
	void testAddExistingTagToTutorial() {

		// El cuerpo de la peticion solo trae el id de la etiqueta
		Tag peticion = Tag.builder().id(5L).build();

		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial));
		given(tagDao.findById(5L)).willReturn(Optional.of(tagExistente));
		given(tutorialDao.save(tutorial)).willReturn(tutorial);

		Tag resultado = tagService.addTagToTutorial(1L, peticion);

		assertThat(resultado).isSameAs(tagExistente);
		assertThat(tutorial.getTags()).contains(tagExistente);
		verify(tagDao, never()).save(any(Tag.class));
	}

	@Test
	@DisplayName("Test del servicio: no se puede crear una etiqueta sin nombre")
	void testAddTagWithoutName() {

		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial));

		assertThatThrownBy(() -> tagService.addTagToTutorial(1L, new Tag()))
				.isInstanceOf(BadRequestException.class);

		verify(tagDao, never()).save(any(Tag.class));
	}

	@Test
	@DisplayName("Test del servicio: asociar una etiqueta a un tutorial que no existe")
	void testAddTagToTutorialNotFound() {

		given(tutorialDao.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> tagService.addTagToTutorial(99L, tagExistente))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	@DisplayName("Test del servicio para quitar una etiqueta de un tutorial")
	void testRemoveTagFromTutorial() {

		tutorial.addTag(tagExistente);
		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial));

		tagService.removeTagFromTutorial(1L, 5L);

		assertThat(tutorial.getTags()).isEmpty();
		assertThat(tagExistente.getTutorials()).isEmpty();
		verify(tutorialDao).save(tutorial);
	}

	@Test
	@DisplayName("Test del servicio: quitar una etiqueta que el tutorial no tiene")
	void testRemoveTagNotInTutorial() {

		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial));

		assertThatThrownBy(() -> tagService.removeTagFromTutorial(1L, 5L))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(tutorialDao, never()).save(any(Tutorial.class));
	}

	@Test
	@DisplayName("Test del servicio: al eliminar una etiqueta se desvincula de sus tutoriales")
	void testDeleteTagUnlinksTutorials() {

		tutorial.addTag(tagExistente);
		given(tagDao.findById(5L)).willReturn(Optional.of(tagExistente));

		tagService.deleteById(5L);

		assertThat(tutorial.getTags()).isEmpty();
		verify(tagDao).delete(tagExistente);
	}

	@Test
	@DisplayName("Test del servicio para recuperar las etiquetas de un tutorial que no existe")
	void testFindByTutorialIdNotFound() {

		given(tutorialDao.existsById(99L)).willReturn(false);

		assertThatThrownBy(() -> tagService.findByTutorialId(99L))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(tagDao, never()).findTagsByTutorialsId(any());
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tutoriales de una etiqueta")
	void testFindTutorialsByTagId() {

		given(tagDao.existsById(5L)).willReturn(true);
		given(tutorialDao.findTutorialsByTagsId(5L)).willReturn(List.of(tutorial));

		List<Tutorial> resultado = tagService.findTutorialsByTagId(5L);

		assertThat(resultado).containsExactly(tutorial);
	}

}
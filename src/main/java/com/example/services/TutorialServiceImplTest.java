package com.example.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.dao.TutorialDao;
import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;

/* Tests en aislamiento: el DAO se simula (mock) con Mockito */
@ExtendWith(MockitoExtension.class)
class TutorialServiceImplTest {

	@Mock
	private TutorialDao tutorialDao;

	@InjectMocks
	private TutorialServiceImpl tutorialService;

	private Tutorial tutorial1, tutorial2;

	@BeforeEach
	void setUp() {

		tutorial1 = Tutorial.builder()
				.id(1L)
				.title("Spring Boot")
				.description("Tutorial de Spring Boot")
				.published(true)
				.build();

		tutorial2 = Tutorial.builder()
				.id(2L)
				.title("JPA")
				.description("Tutorial de JPA")
				.published(false)
				.build();
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tutoriales ordenados")
	void testFindAllSort() {

		Sort sort = Sort.by("title");
		given(tutorialDao.findAll(sort)).willReturn(List.of(tutorial2, tutorial1));

		List<Tutorial> result = tutorialService.findAll(sort);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getTitle()).isEqualTo("JPA");
	}

	@Test
	@DisplayName("Test del servicio para recuperar los tutoriales paginados")
	void testFindAllPageable() {

		Pageable pageable = PageRequest.of(0, 2, Sort.by("title"));
		given(tutorialDao.findAll(pageable))
				.willReturn(new PageImpl<>(List.of(tutorial2, tutorial1), pageable, 2));

		Page<Tutorial> result = tutorialService.findAll(pageable);

		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("Test del servicio para buscar tutoriales por titulo")
	void testFindByTitleContaining() {

		Sort sort = Sort.by("title");
		given(tutorialDao.findByTitleContaining("Spring", sort)).willReturn(List.of(tutorial1));

		List<Tutorial> result = tutorialService.findByTitleContaining("Spring", sort);

		assertThat(result).containsExactly(tutorial1);
	}

	@Test
	@DisplayName("Test del servicio para recuperar un tutorial por su id")
	void testFindById() {

		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial1));

		Tutorial result = tutorialService.findById(1L);

		assertThat(result.getTitle()).isEqualTo("Spring Boot");
	}

	@Test
	@DisplayName("Test del servicio cuando el tutorial no existe")
	void testFindByIdNotFound() {

		given(tutorialDao.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> tutorialService.findById(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	@DisplayName("Test del servicio para persistir un tutorial")
	void testSave() {

		given(tutorialDao.save(tutorial1)).willReturn(tutorial1);

		Tutorial guardado = tutorialService.save(tutorial1);

		assertThat(guardado).isNotNull();
		assertThat(guardado.getTitle()).isEqualTo("Spring Boot");
	}

	@Test
	@DisplayName("Test del servicio para actualizar un tutorial")
	void testUpdate() {

		Tutorial cambios = Tutorial.builder()
				.title("Spring Boot 4")
				.description("Descripcion nueva")
				.published(false)
				.build();

		given(tutorialDao.findById(1L)).willReturn(Optional.of(tutorial1));
		given(tutorialDao.save(any(Tutorial.class))).willAnswer(invocation -> invocation.getArgument(0));

		Tutorial actualizado = tutorialService.update(1L, cambios);

		assertThat(actualizado.getId()).isEqualTo(1L);
		assertThat(actualizado.getTitle()).isEqualTo("Spring Boot 4");
		assertThat(actualizado.getDescription()).isEqualTo("Descripcion nueva");
		assertThat(actualizado.getPublished()).isFalse();
	}

	@Test
	@DisplayName("Test del servicio para eliminar un tutorial")
	void testDeleteById() {

		given(tutorialDao.existsById(1L)).willReturn(true);

		tutorialService.deleteById(1L);

		verify(tutorialDao).deleteById(1L);
	}

	@Test
	@DisplayName("Test del servicio para eliminar un tutorial que no existe")
	void testDeleteByIdNotFound() {

		given(tutorialDao.existsById(99L)).willReturn(false);

		assertThatThrownBy(() -> tutorialService.deleteById(99L))
				.isInstanceOf(ResourceNotFoundException.class);

		verify(tutorialDao, never()).deleteById(anyLong());
	}

}

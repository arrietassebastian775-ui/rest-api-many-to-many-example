package com.example.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.entities.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.services.TutorialService;

import tools.jackson.databind.ObjectMapper;

/* @WebMvcTest solo carga la capa web (controladores y @RestControllerAdvice), por eso
 * el servicio se sustituye por un mock. Asi tambien probamos que el manejo de
 * excepciones devuelve los codigos HTTP correctos. */
@WebMvcTest(TutorialController.class)
class TutorialControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockitoBean
	TutorialService tutorialService;

	List<Tutorial> tutorials;
	Tutorial tutorial1, tutorial2;

	@BeforeEach
	void setUp() {

		tutorial1 = Tutorial.builder()
				.id(1L)
				.title("Spring Boot para principiantes")
				.description("Introduccion a Spring Boot")
				.published(true)
				.build();

		tutorial2 = Tutorial.builder()
				.id(2L)
				.title("JPA y Hibernate")
				.description("Persistencia con JPA")
				.published(true)
				.build();

		tutorials = List.of(tutorial2, tutorial1);
	}

	@Test
	@DisplayName("Controller Test que recupera todos los tutoriales")
	void testFindAll() throws Exception {

		given(tutorialService.findAll(Sort.by("title"))).willReturn(tutorials);

		mockMvc.perform(get("/api/tutorials").accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tutorials.size()", is(2)));
	}

	@Test
	@DisplayName("Controller Test que recupera los tutoriales paginados")
	void testFindAllPaginated() throws Exception {

		Pageable pageable = PageRequest.of(0, 2, Sort.by("title"));
		given(tutorialService.findAll(pageable))
				.willReturn(new PageImpl<>(tutorials, pageable, 2));

		mockMvc.perform(get("/api/tutorials?page=0&size=2").accept(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tutorials.size()", is(2)))
				.andExpect(jsonPath("$.currentPage", is(0)))
				.andExpect(jsonPath("$.totalItems", is(2)))
				.andExpect(jsonPath("$.totalPages", is(1)));
	}

	@Test
	@DisplayName("Controller Test que filtra los tutoriales por titulo")
	void testFindByTitle() throws Exception {

		given(tutorialService.findByTitleContaining("Spring", Sort.by("title")))
				.willReturn(List.of(tutorial1));

		mockMvc.perform(get("/api/tutorials").param("title", "Spring"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tutorials.size()", is(1)))
				.andExpect(jsonPath("$.tutorials[0].title", is(tutorial1.getTitle())));
	}

	@Test
	@DisplayName("Controller Test con parametros de paginacion invalidos")
	void testInvalidPagination() throws Exception {

		mockMvc.perform(get("/api/tutorials?page=-1&size=2"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.statusCode", is(400)));
	}

	@Test
	@DisplayName("Controller Test para recuperar un tutorial por su id")
	void testFindById() throws Exception {

		given(tutorialService.findById(1L)).willReturn(tutorial1);

		mockMvc.perform(get("/api/tutorials/{id}", 1L))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is(tutorial1.getTitle())));
	}

	@Test
	@DisplayName("Controller Test tutorial no encontrado")
	void testTutorialNotFound() throws Exception {

		given(tutorialService.findById(99L))
				.willThrow(new ResourceNotFoundException("No se ha encontrado el tutorial con id = 99"));

		mockMvc.perform(get("/api/tutorials/{id}", 99L))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.statusCode", is(404)))
				.andExpect(jsonPath("$.message", is("No se ha encontrado el tutorial con id = 99")));
	}

	@Test
	@DisplayName("Controller Test para persistir un tutorial")
	void testCreateTutorial() throws Exception {

		given(tutorialService.save(any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

		mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tutorial1)))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is(tutorial1.getTitle())))
				.andExpect(jsonPath("$.published", is(true)));
	}

	@Test
	@DisplayName("Controller Test para persistir un tutorial mal formado")
	void testCreateInvalidTutorial() throws Exception {

		Tutorial invalido = Tutorial.builder().title("").description("").build();

		mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalido)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.statusCode", is(400)));

		verify(tutorialService, never()).save(any(Tutorial.class));
	}

	@Test
	@DisplayName("Controller Test con un JSON mal formado")
	void testCreateTutorialWithMalformedJson() throws Exception {

		mockMvc.perform(post("/api/tutorials")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ esto no es json"))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Controller Test para actualizar un tutorial")
	void testUpdateTutorial() throws Exception {

		Tutorial cambios = Tutorial.builder()
				.title("Spring Boot 4")
				.description("Descripcion actualizada")
				.published(false)
				.build();

		given(tutorialService.update(eq(1L), any(Tutorial.class)))
				.willAnswer(invocation -> invocation.getArgument(1));

		mockMvc.perform(put("/api/tutorials/{id}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cambios)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Spring Boot 4")))
				.andExpect(jsonPath("$.published", is(false)));
	}

	@Test
	@DisplayName("Controller Test para eliminar un tutorial")
	void testDeleteTutorial() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{id}", 1L))
				.andDo(print())
				.andExpect(status().isNoContent());

		verify(tutorialService).deleteById(1L);
	}

	@Test
	@DisplayName("Controller Test para eliminar un tutorial que no existe")
	void testDeleteTutorialNotFound() throws Exception {

		doThrow(new ResourceNotFoundException("No se ha encontrado el tutorial con id = 99"))
				.when(tutorialService).deleteById(99L);

		mockMvc.perform(delete("/api/tutorials/{id}", 99L))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Controller Test: una ruta inexistente responde 404 y no 500")
	void testUnknownRoute() throws Exception {

		mockMvc.perform(get("/api/no-existe"))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

}

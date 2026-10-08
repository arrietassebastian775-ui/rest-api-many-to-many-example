package com.example.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.entities.Tag;
import com.example.entities.Tutorial;
import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;
import com.example.services.TagService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TagController.class)
class TagControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockitoBean
	TagService tagService;

	Tag tag1, tag2;
	Tutorial tutorial1;

	@BeforeEach
	void setUp() {

		tag1 = Tag.builder().id(1L).name("java").build();
		tag2 = Tag.builder().id(2L).name("spring").build();

		tutorial1 = Tutorial.builder()
				.id(1L)
				.title("Spring Boot para principiantes")
				.description("Introduccion a Spring Boot")
				.published(true)
				.build();
	}

	@Test
	@DisplayName("Controller Test que recupera todas las etiquetas")
	void testFindAllTags() throws Exception {

		given(tagService.findAll()).willReturn(List.of(tag1, tag2));

		mockMvc.perform(get("/api/tags"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()", is(2)))
				.andExpect(jsonPath("$[0].name", is("java")));
	}

	@Test
	@DisplayName("Controller Test sin etiquetas responde 204")
	void testFindAllTagsEmpty() throws Exception {

		given(tagService.findAll()).willReturn(List.of());

		mockMvc.perform(get("/api/tags"))
				.andDo(print())
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Controller Test etiqueta no encontrada")
	void testTagNotFound() throws Exception {

		given(tagService.findById(99L))
				.willThrow(new ResourceNotFoundException("No se ha encontrado la etiqueta con id = 99"));

		mockMvc.perform(get("/api/tags/{id}", 99L))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.statusCode", is(404)));
	}

	@Test
	@DisplayName("Controller Test que recupera las etiquetas de un tutorial")
	void testFindTagsByTutorialId() throws Exception {

		given(tagService.findByTutorialId(1L)).willReturn(List.of(tag1, tag2));

		mockMvc.perform(get("/api/tutorials/{tutorialId}/tags", 1L))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size()", is(2)));
	}

	@Test
	@DisplayName("Controller Test que recupera los tutoriales de una etiqueta")
	void testFindTutorialsByTagId() throws Exception {

		given(tagService.findTutorialsByTagId(1L)).willReturn(List.of(tutorial1));

		mockMvc.perform(get("/api/tags/{tagId}/tutorials", 1L))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title", is(tutorial1.getTitle())));
	}

	@Test
	@DisplayName("Controller Test para agregar una etiqueta nueva a un tutorial")
	void testAddNewTag() throws Exception {

		given(tagService.addTagToTutorial(eq(1L), any(Tag.class)))
				.willAnswer(invocation -> invocation.getArgument(1));

		mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"testing\"}"))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("testing")));
	}

	@Test
	@DisplayName("Controller Test para agregar una etiqueta existente enviando solo su id")
	void testAddExistingTagById() throws Exception {

		given(tagService.addTagToTutorial(eq(1L), any(Tag.class))).willReturn(tag2);

		mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"id\": 2}"))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("spring")));
	}

	@Test
	@DisplayName("Controller Test para agregar una etiqueta sin nombre")
	void testAddTagWithoutName() throws Exception {

		given(tagService.addTagToTutorial(eq(1L), any(Tag.class)))
				.willThrow(new BadRequestException("El nombre de la etiqueta es obligatorio"));

		mockMvc.perform(post("/api/tutorials/{tutorialId}/tags", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("El nombre de la etiqueta es obligatorio")));
	}

	@Test
	@DisplayName("Controller Test para actualizar una etiqueta")
	void testUpdateTag() throws Exception {

		given(tagService.update(eq(1L), any(Tag.class)))
				.willAnswer(invocation -> invocation.getArgument(1));

		mockMvc.perform(put("/api/tags/{id}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name("java-25").build())))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("java-25")));
	}

	@Test
	@DisplayName("Controller Test para actualizar una etiqueta con nombre invalido")
	void testUpdateTagInvalidName() throws Exception {

		mockMvc.perform(put("/api/tags/{id}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Tag.builder().name(" ").build())))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.statusCode", is(400)));

		verify(tagService, never()).update(eq(1L), any(Tag.class));
	}

	@Test
	@DisplayName("Controller Test para eliminar una etiqueta")
	void testDeleteTag() throws Exception {

		mockMvc.perform(delete("/api/tags/{id}", 1L))
				.andDo(print())
				.andExpect(status().isNoContent());

		verify(tagService).deleteById(1L);
	}

	@Test
	@DisplayName("Controller Test para quitar una etiqueta de un tutorial")
	void testRemoveTagFromTutorial() throws Exception {

		mockMvc.perform(delete("/api/tutorials/{tutorialId}/tags/{tagId}", 1L, 2L))
				.andDo(print())
				.andExpect(status().isNoContent());

		verify(tagService).removeTagFromTutorial(1L, 2L);
	}

}

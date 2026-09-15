package ru.vitalii.task_manager.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.vitalii.task_manager.dto.ProjectRequest;
import ru.vitalii.task_manager.dto.ProjectResponse;
import ru.vitalii.task_manager.exception.GlobalExceptionHandler;
import ru.vitalii.task_manager.model.enums.ProjectStatus;
import ru.vitalii.task_manager.service.ProjectService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(GlobalExceptionHandler.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private ProjectRequest request;
    private ProjectResponse response;

    @BeforeEach
    void setUp() {
        request = ProjectRequest.builder()
                .name("Проект А")
                .description("Велосипед")
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ProjectStatus.PLANNED)
                .build();

        response = ProjectResponse.builder()
                .id(1L)
                .name("Проект А")
                .description("Велосипед")
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ProjectStatus.PLANNED)
                .build();
    }

    // ------------------------------------------------------------------
    // GET /projects
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /projects")
    class GetAll {

        @Test
        @DisplayName("возвращает 200 и список проектов")
        void returnsList() throws Exception {
            when(projectService.findAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Проект А"))
                    .andExpect(jsonPath("$[0].status").value("PLANNED"));

            verify(projectService).findAll();
        }

        @Test
        @DisplayName("возвращает 200 и пустой список")
        void returnsEmptyList() throws Exception {
            when(projectService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // GET /projects/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /projects/{id}")
    class GetById {

        @Test
        @DisplayName("возвращает 200 и DTO проекта")
        void returnsProject() throws Exception {
            when(projectService.findById(1L)).thenReturn(response);

            mockMvc.perform(get("/projects/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Проект А"))
                    .andExpect(jsonPath("$.status").value("PLANNED"));

            verify(projectService).findById(1L);
        }

        @Test
        @DisplayName("возвращает 404, если проект не найден")
        void returnsNotFound() throws Exception {
            when(projectService.findById(999L))
                    .thenThrow(new EntityNotFoundException("Не найден проект с id: 999"));

            mockMvc.perform(get("/projects/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Не найден проект с id: 999"));
        }

        @Test
        @DisplayName("возвращает 400 при нечисловом id")
        void returnsBadRequestOnInvalidId() throws Exception {
            mockMvc.perform(get("/projects/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(projectService, never()).findById(any());
        }
    }

    // ------------------------------------------------------------------
    // POST /projects
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /projects")
    class Create {

        @Test
        @DisplayName("возвращает 201 и созданный DTO")
        void creates() throws Exception {
            when(projectService.create(any(ProjectRequest.class))).thenReturn(response);

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Проект А"));

            verify(projectService).create(any(ProjectRequest.class));
        }

        @Test
        @DisplayName("возвращает 400 при пустом name")
        void rejectsBlankName() throws Exception {
            request.setName("");

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("name")));

            verify(projectService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при отсутствии startDate")
        void rejectsMissingStartDate() throws Exception {
            request.setStartDate(null);

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("startDate")));

            verify(projectService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при отсутствии status")
        void rejectsMissingStatus() throws Exception {
            request.setStatus(null);

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(projectService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при неизвестном status в JSON")
        void rejectsUnknownStatus() throws Exception {
            String json = """
                    {
                      "name": "Проект А",
                      "startDate": "2026-01-15",
                      "status": "невалидный_статус"
                    }
                    """;

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(projectService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при нечитаемом JSON")
        void rejectsMalformedJson() throws Exception {
            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ неверно }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(projectService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400, если сервис бросает IllegalArgumentException (endDate < startDate)")
        void returnsBadRequestOnInvalidDates() throws Exception {
            when(projectService.create(any(ProjectRequest.class)))
                    .thenThrow(new IllegalArgumentException("Дата окончания не может быть раньше даты начала"));

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Дата окончания не может быть раньше даты начала"));
        }

        @Test
        @DisplayName("принимает проект без endDate")
        void acceptsNullEndDate() throws Exception {
            ProjectRequest noEndDate = ProjectRequest.builder()
                    .name("Проект А")
                    .startDate(LocalDate.of(2026, 1, 15))
                    .status(ProjectStatus.PLANNED)
                    .build();

            ProjectResponse noEndDateResponse = ProjectResponse.builder()
                    .id(1L)
                    .name("Проект А")
                    .startDate(LocalDate.of(2026, 1, 15))
                    .endDate(null)
                    .status(ProjectStatus.PLANNED)
                    .build();

            when(projectService.create(any(ProjectRequest.class))).thenReturn(noEndDateResponse);

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noEndDate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.endDate").doesNotExist());
        }
    }

    // ------------------------------------------------------------------
    // PUT /projects/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /projects/{id}")
    class Update {

        @Test
        @DisplayName("возвращает 200 и обновлённый DTO")
        void updates() throws Exception {
            when(projectService.update(eq(1L), any(ProjectRequest.class))).thenReturn(response);

            mockMvc.perform(put("/projects/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Проект А"));

            verify(projectService).update(eq(1L), any(ProjectRequest.class));
        }

        @Test
        @DisplayName("возвращает 404, если проект не найден")
        void returnsNotFound() throws Exception {
            when(projectService.update(eq(999L), any(ProjectRequest.class)))
                    .thenThrow(new EntityNotFoundException("Не найден проект с id: 999"));

            mockMvc.perform(put("/projects/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном DTO")
        void rejectsInvalidDto() throws Exception {
            request.setName("");

            mockMvc.perform(put("/projects/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(projectService, never()).update(any(), any());
        }

        @Test
        @DisplayName("возвращает 400, если сервис бросает IllegalArgumentException")
        void returnsBadRequestOnInvalidDates() throws Exception {
            when(projectService.update(eq(1L), any(ProjectRequest.class)))
                    .thenThrow(new IllegalArgumentException("Дата окончания не может быть раньше даты начала"));

            mockMvc.perform(put("/projects/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Дата окончания не может быть раньше даты начала"));
        }
    }

    // ------------------------------------------------------------------
    // DELETE /projects/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /projects/{id}")
    class Delete {

        @Test
        @DisplayName("возвращает 204 при успешном удалении")
        void deletes() throws Exception {
            mockMvc.perform(delete("/projects/1"))
                    .andExpect(status().isNoContent());

            verify(projectService).delete(1L);
        }

        @Test
        @DisplayName("возвращает 404, если проект не найден")
        void returnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Не найден проект с id: 999"))
                    .when(projectService).delete(999L);

            mockMvc.perform(delete("/projects/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Не найден проект с id: 999"));
        }

        @Test
        @DisplayName("возвращает 400 при нечисловом id")
        void returnsBadRequestOnInvalidId() throws Exception {
            mockMvc.perform(delete("/projects/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(projectService, never()).delete(any());
        }
    }
}

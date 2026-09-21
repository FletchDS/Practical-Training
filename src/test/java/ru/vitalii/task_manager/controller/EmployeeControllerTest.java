package ru.vitalii.task_manager.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.vitalii.task_manager.dto.EmployeeRequest;
import ru.vitalii.task_manager.dto.EmployeeResponse;
import ru.vitalii.task_manager.dto.SkillResponse;
import ru.vitalii.task_manager.exception.GlobalExceptionHandler;
import ru.vitalii.task_manager.security.JwtService;
import ru.vitalii.task_manager.service.EmployeeService;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private EmployeeRequest request;
    private EmployeeResponse response;

    @BeforeEach
    void setUp() {
        request = EmployeeRequest.builder()
                .firstName("Иван")
                .lastName("Петров")
                .email("ivan@example.com")
                .phone("+79001234567")
                .hireDate(LocalDate.of(2024, 1, 15))
                .positionId(1L)
                .build();

        response = EmployeeResponse.builder()
                .id(100L)
                .firstName("Иван")
                .lastName("Петров")
                .email("ivan@example.com")
                .phone("+79001234567")
                .hireDate(LocalDate.of(2024, 1, 15))
                .positionId(1L)
                .positionTitle("Developer")
                .skills(Set.of())
                .build();
    }

    // ------------------------------------------------------------------
    // GET /employees
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /employees")
    class GetAll {

        @Test
        @DisplayName("возвращает 200 и список сотрудников")
        void returnsList() throws Exception {
            when(employeeService.findAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(100))
                    .andExpect(jsonPath("$[0].firstName").value("Иван"))
                    .andExpect(jsonPath("$[0].email").value("ivan@example.com"));

            verify(employeeService).findAll();
        }

        @Test
        @DisplayName("возвращает 200 и пустой список")
        void returnsEmptyList() throws Exception {
            when(employeeService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // GET /employees/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /employees/{id}")
    class GetById {

        @Test
        @DisplayName("возвращает 200 и DTO сотрудника")
        void returnsEmployee() throws Exception {
            when(employeeService.findById(100L)).thenReturn(response);

            mockMvc.perform(get("/employees/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.firstName").value("Иван"))
                    .andExpect(jsonPath("$.positionTitle").value("Developer"));

            verify(employeeService).findById(100L);
        }

        @Test
        @DisplayName("возвращает 404, если сотрудник не найден")
        void returnsNotFound() throws Exception {
            when(employeeService.findById(999L))
                    .thenThrow(new EntityNotFoundException("Не найден сотрудник с id: 999"));

            mockMvc.perform(get("/employees/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Не найден сотрудник с id: 999"));
        }

        @Test
        @DisplayName("возвращает 400 при нечисловом id")
        void returnsBadRequestOnInvalidId() throws Exception {
            mockMvc.perform(get("/employees/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(employeeService, never()).findById(any());
        }
    }

    // ------------------------------------------------------------------
    // POST /employees
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /employees")
    class Create {

        @Test
        @DisplayName("возвращает 201 и созданный DTO")
        void creates() throws Exception {
            when(employeeService.create(any(EmployeeRequest.class))).thenReturn(response);

            mockMvc.perform(post("/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.email").value("ivan@example.com"));

            verify(employeeService).create(any(EmployeeRequest.class));
        }

        @Test
        @DisplayName("возвращает 400 при пустом firstName")
        void rejectsBlankFirstName() throws Exception {
            request.setFirstName("");

            mockMvc.perform(post("/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("firstName")));

            verify(employeeService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при невалидном email")
        void rejectsInvalidEmail() throws Exception {
            request.setEmail("не email");

            mockMvc.perform(post("/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email")));

            verify(employeeService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400, если все обязательные поля пусты")
        void rejectsAllMissingFields() throws Exception {
            EmployeeRequest invalid = EmployeeRequest.builder().build();

            mockMvc.perform(post("/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(employeeService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400 при нечитаемом JSON")
        void rejectsMalformedJson() throws Exception {
            mockMvc.perform(post("/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ неверно }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(employeeService, never()).create(any());
        }

        @Test
        @DisplayName("возвращает 400, если сервис бросает IllegalArgumentException")
        void returnsBadRequestOnServiceException() throws Exception {
            when(employeeService.create(any(EmployeeRequest.class)))
                    .thenThrow(new IllegalArgumentException("Email уже существует"));

            mockMvc.perform(post("/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Email уже существует"));
        }
    }

    // ------------------------------------------------------------------
    // PUT /employees/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /employees/{id}")
    class Update {

        @Test
        @DisplayName("возвращает 200 и обновлённый DTO")
        void updates() throws Exception {
            when(employeeService.update(eq(100L), any(EmployeeRequest.class))).thenReturn(response);

            mockMvc.perform(put("/employees/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100));

            verify(employeeService).update(eq(100L), any(EmployeeRequest.class));
        }

        @Test
        @DisplayName("возвращает 404, если сотрудник не найден")
        void returnsNotFound() throws Exception {
            when(employeeService.update(eq(999L), any(EmployeeRequest.class)))
                    .thenThrow(new EntityNotFoundException("Не найдун сотрудник с id: 999"));

            mockMvc.perform(put("/employees/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("возвращает 400 при невалидном DTO")
        void rejectsInvalidDto() throws Exception {
            request.setEmail("неверно");

            mockMvc.perform(put("/employees/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(employeeService, never()).update(any(), any());
        }
    }

    // ------------------------------------------------------------------
    // DELETE /employees/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /employees/{id}")
    class Delete {

        @Test
        @DisplayName("возвращает 204 при успешном удалении")
        void deletes() throws Exception {
            mockMvc.perform(delete("/employees/100"))
                    .andExpect(status().isNoContent());

            verify(employeeService).deleteById(100L);
        }

        @Test
        @DisplayName("возвращает 404, если сотрудник не найден")
        void returnsNotFound() throws Exception {
            org.mockito.Mockito.doThrow(new EntityNotFoundException("Не найден сотрудник с id: 999"))
                    .when(employeeService).deleteById(999L);

            mockMvc.perform(delete("/employees/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // ------------------------------------------------------------------
    // POST /employees/{employeeId}/skills/{skillId}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /employees/{employeeId}/skills/{skillId}")
    class AddSkill {

        @Test
        @DisplayName("возвращает 200 и обновлённый DTO")
        void addsSkill() throws Exception {
            SkillResponse skill = SkillResponse.builder()
                    .id(10L)
                    .name("Java")
                    .build();
            EmployeeResponse withSkill = EmployeeResponse.builder()
                    .id(100L)
                    .firstName("Иван")
                    .lastName("Петров")
                    .email("ivan@example.com")
                    .skills(Set.of(skill))
                    .build();

            when(employeeService.addSkill(100L, 10L)).thenReturn(withSkill);

            mockMvc.perform(post("/employees/100/skills/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.skills[0].id").value(10))
                    .andExpect(jsonPath("$.skills[0].name").value("Java"));

            verify(employeeService).addSkill(100L, 10L);
        }

        @Test
        @DisplayName("возвращает 404, если сотрудник не найден")
        void returnsNotFoundWhenEmployeeMissing() throws Exception {
            when(employeeService.addSkill(999L, 10L))
                    .thenThrow(new EntityNotFoundException("Не найден сотрудник с id: 999"));

            mockMvc.perform(post("/employees/999/skills/10"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("возвращает 404, если навык не найден")
        void returnsNotFoundWhenSkillMissing() throws Exception {
            when(employeeService.addSkill(100L, 999L))
                    .thenThrow(new EntityNotFoundException("Не найден навык с id: 999"));

            mockMvc.perform(post("/employees/100/skills/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------------
    // DELETE /employees/{employeeId}/skills/{skillId}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /employees/{employeeId}/skills/{skillId}")
    class RemoveSkill {

        @Test
        @DisplayName("возвращает 200 и обновлённый DTO без навыка")
        void removesSkill() throws Exception {
            when(employeeService.removeSkill(100L, 10L)).thenReturn(response);

            mockMvc.perform(delete("/employees/100/skills/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.skills").isEmpty());

            verify(employeeService).removeSkill(100L, 10L);
        }

        @Test
        @DisplayName("возвращает 404, если сотрудник не найден")
        void returnsNotFoundWhenEmployeeMissing() throws Exception {
            when(employeeService.removeSkill(999L, 10L))
                    .thenThrow(new EntityNotFoundException("Не найден сотрудник с id: 999"));

            mockMvc.perform(delete("/employees/999/skills/10"))
                    .andExpect(status().isNotFound());
        }
    }
}

package ru.vitalii.task_manager.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vitalii.task_manager.dto.EmployeeRequest;
import ru.vitalii.task_manager.dto.EmployeeResponse;
import ru.vitalii.task_manager.mapper.EmployeeMapper;
import ru.vitalii.task_manager.model.Employee;
import ru.vitalii.task_manager.model.Position;
import ru.vitalii.task_manager.model.Skill;
import ru.vitalii.task_manager.repository.EmployeeRepository;
import ru.vitalii.task_manager.repository.SkillRepository;
import ru.vitalii.task_manager.service.imp.EmployeeServiceImp;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private PositionService positionService;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImp employeeService;

    private Employee employee;
    private EmployeeResponse employeeResponse;
    private EmployeeRequest employeeRequest;
    private Skill skill;
    private Position position;

    @BeforeEach
    void setUp() {
        position = new Position();
        position.setId(1L);
        position.setTitle("Разработчик");

        skill = new Skill();
        skill.setId(10L);
        skill.setName("Java");

        employee = new Employee();
        employee.setId(100L);
        employee.setFirstName("Иван");
        employee.setLastName("Иванов");
        employee.setEmail("ivan@example.com");
        employee.setHireDate(LocalDate.of(2024, 1, 15));
        employee.setPosition(position);
        employee.setSkills(new HashSet<>());

        employeeResponse = EmployeeResponse.builder()
                .id(100L)
                .firstName("Иван")
                .lastName("Иванов")
                .email("ivan@example.com")
                .hireDate(LocalDate.of(2024, 1, 15))
                .positionId(1L)
                .positionTitle("Разработчик")
                .skills(Set.of())
                .build();

        employeeRequest = EmployeeRequest.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .email("ivan@example.com")
                .hireDate(LocalDate.of(2024, 1, 15))
                .positionId(1L)
                .build();
    }

    // ------------------------------------------------------------------
    // findById
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("возвращает DTO, если сотрудник найден")
        void returnsDtoWhenFound() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.findById(100L);

            assertThat(result).isEqualTo(employeeResponse);
            verify(employeeRepository).findById(100L);
            verify(employeeMapper).toResponse(employee);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если сотрудник не найден")
        void throwsWhenNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.findById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("999");

            verify(employeeMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null id")
        void throwsOnNullId() {
            assertThatThrownBy(() -> employeeService.findById(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(employeeRepository, never()).findById(any());
        }
    }

    // ------------------------------------------------------------------
    // findAll
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("возвращает список DTO")
        void returnsListOfDtos() {
            EmployeeResponse response = EmployeeResponse.builder()
                    .id(100L)
                    .firstName("Ivan")
                    .lastName("Petrov")
                    .email("ivan@example.com")
                    .positionId(1L)
                    .positionTitle("Developer")
                    .skills(Set.of())
                    .build();

            when(employeeRepository.findAllWithDetails()).thenReturn(List.of(employee));
            when(employeeMapper.toResponse(employee)).thenReturn(response);

            List<EmployeeResponse> result = employeeService.findAll();

            assertThat(result).containsExactly(response);
            verify(employeeRepository).findAllWithDetails();
            verify(employeeMapper).toResponse(employee);
        }

        @Test
        @DisplayName("возвращает пустой список, если сотрудников нет")
        void returnsEmptyList() {
            when(employeeRepository.findAllWithDetails()).thenReturn(List.of());

            assertThat(employeeService.findAll()).isEmpty();

            verify(employeeRepository).findAllWithDetails();
            verify(employeeMapper, never()).toResponse(any());
        }
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт сотрудника с должностью")
        void createsEmployeeWithPosition() {
            when(employeeMapper.toEntity(eq(employeeRequest), any())).thenReturn(employee);
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.create(employeeRequest);

            assertThat(result).isEqualTo(employeeResponse);
            verify(employeeMapper).toEntity(eq(employeeRequest), any());
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("создаёт сотрудника без должности, если positionId не указан")
        void createsEmployeeWithoutPosition() {
            EmployeeRequest requestNoPosition = EmployeeRequest.builder()
                    .firstName("Иван")
                    .lastName("Иванов")
                    .email("ivan@example.com")
                    .build();

            Employee employeeNoPosition = new Employee();
            employeeNoPosition.setFirstName("Иван");
            employeeNoPosition.setLastName("Иванов");
            employeeNoPosition.setEmail("ivan@example.com");

            EmployeeResponse responseNoPosition = EmployeeResponse.builder()
                    .id(100L)
                    .firstName("Иван")
                    .lastName("Иванов")
                    .email("ivan@example.com")
                    .positionId(null)
                    .positionTitle(null)
                    .skills(Set.of())
                    .build();

            when(employeeMapper.toEntity(eq(requestNoPosition), any())).thenReturn(employeeNoPosition);
            when(employeeRepository.save(employeeNoPosition)).thenReturn(employeeNoPosition);
            when(employeeMapper.toResponse(employeeNoPosition)).thenReturn(responseNoPosition);

            EmployeeResponse result = employeeService.create(requestNoPosition);

            assertThat(result).isEqualTo(responseNoPosition);
            assertThat(result.getPositionId()).isNull();
            verify(positionService, never()).findById(any());
            verify(employeeRepository).save(employeeNoPosition);
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null request")
        void throwsOnNullRequest() {
            assertThatThrownBy(() -> employeeService.create(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при дубликате email")
        void throwsOnDuplicateEmail() {
            when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> employeeService.create(employeeRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Email");

            verify(employeeRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("обновляет сотрудника и меняет фамилию")
        void updatesEmployeeAndPosition() {
            EmployeeRequest updateRequest = EmployeeRequest.builder()
                    .firstName("Иван")
                    .lastName("Sidorov")
                    .email("ivan@example.com")
                    .positionId(1L)
                    .build();

            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(positionService.findById(1L)).thenReturn(position);
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.update(100L, updateRequest);

            assertThat(result).isEqualTo(employeeResponse);
            verify(employeeMapper).updateEntity(employee, updateRequest);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("сбрасывает должность, если positionId не передан")
        void clearsPositionWhenPositionIdNull() {
            EmployeeRequest updateRequest = EmployeeRequest.builder()
                    .firstName("Иван")
                    .lastName("Иванов")
                    .email("ivan@example.com")
                    .positionId(null)
                    .build();

            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            employeeService.update(100L, updateRequest);

            assertThat(employee.getPosition()).isNull();
            verify(positionService, never()).findById(any());
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если сотрудник не найден")
        void throwsWhenNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.update(999L, employeeRequest))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при дубликате email")
        void throwsOnDuplicateEmail() {
            EmployeeRequest updateRequest = EmployeeRequest.builder()
                    .firstName("Иван")
                    .lastName("Иванов")
                    .email("other@example.com")
                    .build();

            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(employeeRepository.existsByEmail("other@example.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.update(100L, updateRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Email");

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("не проверяет уникальность email, если email не изменился")
        void skipsEmailCheckWhenUnchanged() {
            EmployeeRequest sameEmailRequest = EmployeeRequest.builder()
                    .firstName("Иван")
                    .lastName("Иванов")
                    .email("ivan@example.com")
                    .build();

            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            employeeService.update(100L, sameEmailRequest);

            verify(employeeRepository, never()).existsByEmail(any());
        }
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("удаляет существующего сотрудника")
        void deletesExisting() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

            employeeService.deleteById(100L);

            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если сотрудник не найден")
        void throwsWhenNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteById(999L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(employeeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null id")
        void throwsOnNullId() {
            assertThatThrownBy(() -> employeeService.deleteById(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // addSkill
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("addSkill")
    class AddSkill {

        @Test
        @DisplayName("добавляет навык сотруднику")
        void addsSkill() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.addSkill(100L, 10L);

            assertThat(result).isEqualTo(employeeResponse);
            assertThat(employee.getSkills()).containsExactly(skill);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("повторное добавление навыка пользователя и возвращение пользователя без изменений")
        void isIdempotent() {
            employee.getSkills().add(skill);

            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.addSkill(100L, 10L);

            assertThat(result).isEqualTo(employeeResponse);
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если сотрудник не найден")
        void throwsWhenEmployeeNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.addSkill(999L, 10L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если навык не найден")
        void throwsWhenSkillNotFound() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.addSkill(100L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null employeeId")
        void throwsOnNullEmployeeId() {
            assertThatThrownBy(() -> employeeService.addSkill(null, 10L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null skillId")
        void throwsOnNullSkillId() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

            assertThatThrownBy(() -> employeeService.addSkill(100L, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // removeSkill
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("removeSkill")
    class RemoveSkill {

        @Test
        @DisplayName("удаляет навык у сотрудника")
        void removesSkill() {
            employee.getSkills().add(skill);

            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            employeeService.removeSkill(100L, 10L);

            assertThat(employee.getSkills()).isEmpty();
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("удаление несуществующего навыка не падает")
        void isIdempotent() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(10L)).thenReturn(Optional.of(skill));
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            employeeService.removeSkill(100L, 10L);

            assertThat(employee.getSkills()).isEmpty();
            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если сотрудник не найден")
        void throwsWhenEmployeeNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.removeSkill(999L, 10L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если навык не найден")
        void throwsWhenSkillNotFound() {
            when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
            when(skillRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.removeSkill(100L, 999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}

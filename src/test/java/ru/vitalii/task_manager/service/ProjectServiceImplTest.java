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
import ru.vitalii.task_manager.dto.ProjectRequest;
import ru.vitalii.task_manager.dto.ProjectResponse;
import ru.vitalii.task_manager.mapper.ProjectMapper;
import ru.vitalii.task_manager.model.Project;
import ru.vitalii.task_manager.model.enums.ProjectStatus;
import ru.vitalii.task_manager.repository.ProjectRepository;
import ru.vitalii.task_manager.service.imp.ProjectServiceImp;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImp projectService;

    private Project project;
    private ProjectResponse projectResponse;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Проект А");
        project.setDescription("Велосипед");
        project.setStartDate(LocalDate.of(2026, 1, 15));
        project.setEndDate(LocalDate.of(2026, 6, 30));
        project.setStatus(ProjectStatus.PLANNED);

        projectResponse = ProjectResponse.builder()
                .id(1L)
                .name("Проект А")
                .description("Велосипед")
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ProjectStatus.PLANNED)
                .build();

        projectRequest = ProjectRequest.builder()
                .name("Проект А")
                .description("Велосипед")
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ProjectStatus.PLANNED)
                .build();
    }

    // ------------------------------------------------------------------
    // findById
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("возвращает DTO, если проект найден")
        void returnsDtoWhenFound() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse result = projectService.findById(1L);

            assertThat(result).isEqualTo(projectResponse);
            verify(projectRepository).findById(1L);
            verify(projectMapper).toResponse(project);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если проект не найден")
        void throwsWhenNotFound() {
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.findById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("999");

            verify(projectMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null id")
        void throwsOnNullId() {
            assertThatThrownBy(() -> projectService.findById(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(projectRepository, never()).findById(any());
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
            when(projectRepository.findAll()).thenReturn(List.of(project));
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            List<ProjectResponse> result = projectService.findAll();

            assertThat(result).containsExactly(projectResponse);
            verify(projectRepository).findAll();
        }

        @Test
        @DisplayName("возвращает пустой список, если проектов нет")
        void returnsEmptyList() {
            when(projectRepository.findAll()).thenReturn(List.of());

            assertThat(projectService.findAll()).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт проект с валидными датами")
        void createsProject() {
            when(projectMapper.toEntity(projectRequest)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse result = projectService.create(projectRequest);

            assertThat(result).isEqualTo(projectResponse);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("создаёт проект без endDate")
        void createsProjectWithoutEndDate() {
            ProjectRequest requestNoEndDate = ProjectRequest.builder()
                    .name("Проект А")
                    .startDate(LocalDate.of(2026, 1, 15))
                    .status(ProjectStatus.PLANNED)
                    .build();

            Project projectNoEnd = new Project();
            projectNoEnd.setName("Проект А");
            projectNoEnd.setStartDate(LocalDate.of(2026, 1, 15));
            projectNoEnd.setStatus(ProjectStatus.PLANNED);

            when(projectMapper.toEntity(requestNoEndDate)).thenReturn(projectNoEnd);
            when(projectRepository.save(projectNoEnd)).thenReturn(projectNoEnd);
            when(projectMapper.toResponse(projectNoEnd)).thenReturn(projectResponse);

            projectService.create(requestNoEndDate);

            verify(projectRepository).save(projectNoEnd);
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при endDate раньше startDate")
        void throwsWhenEndDateBeforeStartDate() {
            Project invalidProject = new Project();
            invalidProject.setName("Проект А");
            invalidProject.setStartDate(LocalDate.of(2026, 6, 30));
            invalidProject.setEndDate(LocalDate.of(2026, 1, 15));

            when(projectMapper.toEntity(projectRequest)).thenReturn(invalidProject);

            assertThatThrownBy(() -> projectService.create(projectRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Дата окончания не должна быть раньше даты начала");

            verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null request")
        void throwsOnNullRequest() {
            assertThatThrownBy(() -> projectService.create(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(projectRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("обновляет проект")
        void updatesProject() {
            ProjectRequest updateRequest = ProjectRequest.builder()
                    .name("Task Manager v2")
                    .description("Updated")
                    .startDate(LocalDate.of(2026, 1, 15))
                    .endDate(LocalDate.of(2026, 8, 31))
                    .status(ProjectStatus.ACTIVE)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse result = projectService.update(1L, updateRequest);

            assertThat(result).isEqualTo(projectResponse);
            verify(projectMapper).updateEntity(project, updateRequest);
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("бросает IllegalArgumentException, если endDate раньше startDate")
        void throwsWhenEndDateBeforeStartDate() {
            ProjectRequest invalidRequest = ProjectRequest.builder()
                    .name("Проект А")
                    .startDate(LocalDate.of(2026, 6, 30))
                    .endDate(LocalDate.of(2026, 1, 15))
                    .status(ProjectStatus.ACTIVE)
                    .build();

            assertThatThrownBy(() -> projectService.update(1L, invalidRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Дата окончания не должна быть раньше даты начала");

            verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("не падает, если endDate null")
        void allowsNullEndDate() {
            ProjectRequest requestNoEndDate = ProjectRequest.builder()
                    .name("Проект А")
                    .startDate(LocalDate.of(2026, 1, 15))
                    .endDate(null)
                    .status(ProjectStatus.ACTIVE)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            projectService.update(1L, requestNoEndDate);

            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если проект не найден")
        void throwsWhenNotFound() {
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.update(999L, projectRequest))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null id")
        void throwsOnNullId() {
            assertThatThrownBy(() -> projectService.update(null, projectRequest))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(projectRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null request")
        void throwsOnNullRequest() {
            assertThatThrownBy(() -> projectService.update(1L, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(projectRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("удаляет существующий проект")
        void deletesExisting() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

            projectService.delete(1L);

            verify(projectRepository).delete(project);
        }

        @Test
        @DisplayName("бросает EntityNotFoundException, если проект не найден")
        void throwsWhenNotFound() {
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.delete(999L))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(projectRepository, never()).delete(any());
        }

        @Test
        @DisplayName("бросает IllegalArgumentException при null id")
        void throwsOnNullId() {
            assertThatThrownBy(() -> projectService.delete(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(projectRepository, never()).delete(any());
        }
    }
}
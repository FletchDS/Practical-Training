package ru.vitalii.task_manager.mapper;

import org.springframework.stereotype.Component;
import ru.vitalii.task_manager.dto.ProjectRequest;
import ru.vitalii.task_manager.dto.ProjectResponse;
import ru.vitalii.task_manager.model.Project;
import ru.vitalii.task_manager.model.enums.ProjectStatus;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .build();
    }

    public Project toEntity(ProjectRequest request) {
        if (request == null) {
            return null;
        }
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNED);

        return project;
    }

    public void updateEntity(Project project, ProjectRequest request) {
        if (project == null || request == null) {
            return;
        }
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
    }
}
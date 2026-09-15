package ru.vitalii.task_manager.service.imp;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vitalii.task_manager.dto.ProjectRequest;
import ru.vitalii.task_manager.dto.ProjectResponse;
import ru.vitalii.task_manager.mapper.ProjectMapper;
import ru.vitalii.task_manager.model.Project;
import ru.vitalii.task_manager.repository.ProjectRepository;
import ru.vitalii.task_manager.service.ProjectService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImp implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponse create(ProjectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос на создание проекта не может быть null");
        }

        Project project = projectMapper.toEntity(request);

        if (project.getEndDate() != null && project.getEndDate().isBefore(project.getStartDate())) {
            throw new IllegalArgumentException("Дата окончания не должна быть раньше даты начала");
        }

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse findById(Long id) {
        return projectMapper.toResponse(findProjectOrThrow(id));
    }

    @Override
    public ProjectResponse update(Long id, ProjectRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("Id проекта не может быть null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Запрос на обновление проекта не может быть null");
        }
        if (request.getEndDate() != null
                && request.getStartDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Дата окончания не должна быть раньше даты начала");
        }

        Project project = findProjectOrThrow(id);

        projectMapper.updateEntity(project, request);

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Project project = findProjectOrThrow(id);
        projectRepository.delete(project);
    }

    @Override
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    private Project findProjectOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id проекта не может быть null");
        }
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отсутствует проект с id: " + id));
    }
}

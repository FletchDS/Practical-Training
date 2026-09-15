package ru.vitalii.task_manager.service;

import ru.vitalii.task_manager.dto.ProjectRequest;
import ru.vitalii.task_manager.dto.ProjectResponse;

import java.util.List;

public interface ProjectService {

    ProjectResponse create(ProjectRequest request);

    ProjectResponse findById(Long id);

    ProjectResponse update(Long id, ProjectRequest request);

    void delete(Long id);

    List<ProjectResponse> findAll();
}
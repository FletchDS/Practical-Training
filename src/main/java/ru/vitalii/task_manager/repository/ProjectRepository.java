package ru.vitalii.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vitalii.task_manager.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {

}

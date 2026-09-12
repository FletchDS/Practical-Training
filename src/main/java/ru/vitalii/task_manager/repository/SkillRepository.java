package ru.vitalii.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vitalii.task_manager.model.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill,Long> {

}

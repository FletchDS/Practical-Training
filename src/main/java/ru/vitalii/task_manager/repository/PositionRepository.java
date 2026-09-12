package ru.vitalii.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vitalii.task_manager.model.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
}

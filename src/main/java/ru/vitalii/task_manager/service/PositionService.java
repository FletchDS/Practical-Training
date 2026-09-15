package ru.vitalii.task_manager.service;

import ru.vitalii.task_manager.model.Position;

public interface PositionService {
    Position findById(Long id);
}

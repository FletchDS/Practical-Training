package ru.vitalii.task_manager.service.imp;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vitalii.task_manager.model.Position;
import ru.vitalii.task_manager.repository.PositionRepository;
import ru.vitalii.task_manager.service.PositionService;

@Service
@RequiredArgsConstructor
public class PositionServiceImp implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public Position findById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отсутствует должность с id: " + id));
    }
}

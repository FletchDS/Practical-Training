package ru.vitalii.task_manager.service.imp;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vitalii.task_manager.dto.EmployeeRequest;
import ru.vitalii.task_manager.dto.EmployeeResponse;
import ru.vitalii.task_manager.mapper.EmployeeMapper;
import ru.vitalii.task_manager.model.Employee;
import ru.vitalii.task_manager.model.Position;
import ru.vitalii.task_manager.model.Skill;
import ru.vitalii.task_manager.model.enums.Role;
import ru.vitalii.task_manager.repository.EmployeeRepository;
import ru.vitalii.task_manager.repository.SkillRepository;
import ru.vitalii.task_manager.service.EmployeeService;
import ru.vitalii.task_manager.service.PositionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImp implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeMapper employeeMapper;
    private final PositionService positionService;

    @Override
    public EmployeeResponse create(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос на создание сотрудника не может быть null");
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email уже существует: " + request.getEmail());
        }
        Employee employee = employeeMapper.toEntity(request, positionService::findById);
        employee.setRole(Role.USER);
        employee.setPassword("");

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return employeeMapper.toResponse(findUserOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findByFirstName(String firstName) {
        if (firstName.isBlank()) {
            throw new IllegalArgumentException("firstName было пустым или равным null");
        }
        Employee employee = employeeRepository.findByFirstName(firstName)
                .orElseThrow(() -> new EntityNotFoundException("Отсутствует сотрудник с именем: " + firstName));
        return employeeMapper.toResponse(employee);
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос на обновление сотрудника не может быть null");
        }

        Employee employee = findUserOrThrow(id);

        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email уже существует: " + request.getEmail());
        }

        employeeMapper.updateEntity(employee, request);

        if (request.getPositionId() != null) {
            Position position = positionService.findById(request.getPositionId());
            employee.setPosition(position);
        } else {
            employee.setPosition(null);
        }

        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public void deleteById(Long id) {
        Employee employee = findUserOrThrow(id);
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAllWithDetails()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeResponse addSkill(Long employeeId, Long skillId) {
        Employee employee = findUserOrThrow(employeeId);
        Skill skill = findSkillOrThrow(skillId);

        if (employee.getSkills().contains(skill)) {
            return employeeMapper.toResponse(employee);
        }

        employee.getSkills().add(skill);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    @Override
    public EmployeeResponse removeSkill(Long employeeId, Long skillId) {
        Employee employee = findUserOrThrow(employeeId);
        Skill skill = findSkillOrThrow(skillId);

        if (!employee.getSkills().contains(skill)) {
            return employeeMapper.toResponse(employee);
        }

        employee.getSkills().remove(skill);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    private Employee findUserOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id  сотрудника не может быть null");
        }
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отсутствует сотрудник с id: " + id));
    }

    private Skill findSkillOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id навыка не может быть null");
        }
        return skillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отсутствует навык с id: " + id));
    }
}

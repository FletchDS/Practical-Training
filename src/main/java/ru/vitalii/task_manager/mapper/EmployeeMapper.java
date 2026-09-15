package ru.vitalii.task_manager.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vitalii.task_manager.dto.EmployeeRequest;
import ru.vitalii.task_manager.dto.EmployeeResponse;
import ru.vitalii.task_manager.dto.SkillResponse;
import ru.vitalii.task_manager.model.Employee;
import ru.vitalii.task_manager.model.Position;

import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final SkillMapper skillMapper;

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }
        Set<SkillResponse> skills = skillMapper.toResponseSet(employee.getSkills());
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .hireDate(employee.getHireDate())
                .positionId(employee.getPosition() != null ? employee.getPosition().getId() : null)
                .positionTitle(employee.getPosition() != null ? employee.getPosition().getTitle() : null)
                .skills(skills)
                .build();
    }

    public void updateEntity(Employee employee, EmployeeRequest request) {
        if (employee == null || request == null) return;
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setHireDate(request.getHireDate());
    }

    public Employee toEntity(EmployeeRequest request) {
        if (request == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setHireDate(request.getHireDate());

        return employee;
    }

    /**
     * @param request          DTO
     * @param positionResolver вернёт Position или null по positionId
     */
    public Employee toEntity(EmployeeRequest request, Function<Long, Position> positionResolver) {
        Employee employee = toEntity(request);
        if (employee != null && request.getPositionId() != null){
            employee.setPosition(positionResolver.apply(request.getPositionId()));
        }
        return employee;
    }
}
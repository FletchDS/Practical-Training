package ru.vitalii.task_manager.service;

import ru.vitalii.task_manager.dto.EmployeeRequest;
import ru.vitalii.task_manager.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse create(EmployeeRequest employee);

    EmployeeResponse findById(Long id);

    EmployeeResponse findByFirstName(String firstName);

    EmployeeResponse update(Long id, EmployeeRequest employee);

    void deleteById(Long id);

    List<EmployeeResponse> findAll();

    EmployeeResponse addSkill(Long employeeId, Long skillId);

    EmployeeResponse removeSkill(Long employeeId, Long skillId);
}


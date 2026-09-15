package ru.vitalii.task_manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vitalii.task_manager.dto.EmployeeRequest;
import ru.vitalii.task_manager.dto.EmployeeResponse;
import ru.vitalii.task_manager.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id,
                                   @Valid @RequestBody EmployeeRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{employeeId}/skills/{skillId}")
    public EmployeeResponse addSkill(@PathVariable Long employeeId,
                                     @PathVariable Long skillId) {
        return employeeService.addSkill(employeeId, skillId);
    }

    @DeleteMapping("/{employeeId}/skills/{skillId}")
    public EmployeeResponse removeSkill(@PathVariable Long employeeId,
                                        @PathVariable Long skillId) {
        return employeeService.removeSkill(employeeId, skillId);
    }
}
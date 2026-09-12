package ru.vitalii.task_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.vitalii.task_manager.model.enums.ProjectStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {

    @NotBlank(message = "Название проекта обязательно")
    @Size(max = 150, message = "Длина имени проекта не должна превышать 150 символов")
    private String name;

    @Size(max = 2000, message = "Длина описания проекта не должна превышать 2000 символов")
    private String description;

    @NotNull(message = "Дата начала проекта обязательна")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Статус проекта обязателен")
    private ProjectStatus status;
}

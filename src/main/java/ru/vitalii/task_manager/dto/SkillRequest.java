package ru.vitalii.task_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRequest {

    @NotBlank(message = "Название навыка обязательно")
    @Size(max = 100, message = "Длина названия навыка не должна превышать 100 символов")
    private String name;

    @Size(max = 1000, message = "Длина описания навыка не должна превышать 1000 символов")
    private String description;
}

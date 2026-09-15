package ru.vitalii.task_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotBlank(message = "Имя обязательно")
    @Size(max = 50, message = "Длина имени не должна превышать 50 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 50, message = "Длина фамилии не должна превышать 50 символов")
    private String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Электронная почта должна быть действительной")
    @Size(max = 100, message = "Длина электронной почты не должна превышать 100 символов")
    private String email;

    @Size(max = 20, message = "Длина номера телефона не должна превышать 20 символов")
    private String phone;

    @PastOrPresent(message = "Дата найма должна быть в прошлом или настоящем")
    private LocalDate hireDate;

    private Long positionId;
}

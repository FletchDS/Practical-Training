package ru.vitalii.task_manager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.vitalii.task_manager.dto.AuthenticationRequest;
import ru.vitalii.task_manager.dto.AuthenticationResponse;
import ru.vitalii.task_manager.dto.RegisterRequest;
import ru.vitalii.task_manager.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return service.authenticate(request);
    }

    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody String refreshToken) {
        return service.refreshToken(refreshToken.trim());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody String refreshToken) {
        service.logout(refreshToken.trim());
    }
}
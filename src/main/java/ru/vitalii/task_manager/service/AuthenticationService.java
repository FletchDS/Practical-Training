package ru.vitalii.task_manager.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vitalii.task_manager.dto.AuthenticationRequest;
import ru.vitalii.task_manager.dto.AuthenticationResponse;
import ru.vitalii.task_manager.dto.RegisterRequest;
import ru.vitalii.task_manager.model.Employee;
import ru.vitalii.task_manager.model.RefreshToken;
import ru.vitalii.task_manager.model.enums.Role;
import ru.vitalii.task_manager.repository.EmployeeRepository;
import ru.vitalii.task_manager.repository.RefreshTokenRepository;
import ru.vitalii.task_manager.config.JwtProperties;
import ru.vitalii.task_manager.security.JwtService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email уже существует: " + request.getEmail());
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        employeeRepository.save(employee);

        String accessToken = jwtService.generateToken(employee);
        RefreshToken refreshToken = createRefreshToken(employee);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден: " + request.getEmail()));

        String accessToken = jwtService.generateToken(employee);
        RefreshToken refreshToken = createRefreshToken(employee);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Токен обновления не найден"));

        verifyRefreshToken(stored);

        Employee employee = stored.getEmployee();
        String newAccessToken = jwtService.generateToken(employee);
        RefreshToken newRefreshToken = createRefreshToken(employee);

        stored.setRevoked(true);
        stored.setReplacedByToken(newRefreshToken.getToken());
        refreshTokenRepository.save(stored);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private RefreshToken createRefreshToken(Employee employee) {
        RefreshToken refreshToken = RefreshToken.builder()
                .employee(employee)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshToken().getExpiration()))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private void verifyRefreshToken(RefreshToken token) {
        if (token.isRevoked()) {
            throw new IllegalStateException("Токен обновления отозван");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalStateException("Срок действия токена обновления истек. Пожалуйста, войдите снова.");
        }
    }
}
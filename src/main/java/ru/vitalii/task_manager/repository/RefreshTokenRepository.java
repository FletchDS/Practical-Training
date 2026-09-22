package ru.vitalii.task_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vitalii.task_manager.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}

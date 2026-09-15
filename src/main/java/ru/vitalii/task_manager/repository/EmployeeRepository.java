package ru.vitalii.task_manager.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.vitalii.task_manager.model.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    Optional<Employee> findByFirstName(String firstName);
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"position", "skills"})
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllWithDetails();
}

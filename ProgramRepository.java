package com.codearena.repository;

import com.codearena.entity.Program;
import com.codearena.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByUserOrderByUpdatedAtDesc(User user);
    Optional<Program> findByIdAndUser(Long id, User user);
}


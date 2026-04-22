package com.codearena.service;

import com.codearena.dto.ProgramDTO;
import com.codearena.entity.Program;
import com.codearena.entity.User;
import com.codearena.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;

    public Program saveProgram(User user, String name, String language, String code) {
        Program program = new Program();
        program.setUser(user);
        program.setName(name);
        program.setLanguage(language);
        program.setCode(code);
        return programRepository.save(program);
    }

    public List<ProgramDTO> getUserPrograms(User user) {
        return programRepository.findByUserOrderByUpdatedAtDesc(user)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public Program getProgramById(Long id, User user) {
        return programRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Program not found"));
    }

    public Program updateProgram(Long id, User user, String name, String language, String code) {
        Program program = getProgramById(id, user);
        program.setName(name);
        program.setLanguage(language);
        program.setCode(code);
        return programRepository.save(program);
    }

    public void deleteProgram(Long id, User user) {
        Program program = getProgramById(id, user);
        programRepository.delete(program);
    }

    public ProgramDTO toDTO(Program program) {
        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setLanguage(program.getLanguage());
        dto.setCode(program.getCode());
        dto.setCreatedAt(program.getCreatedAt() != null ? program.getCreatedAt().toString() : null);
        dto.setUpdatedAt(program.getUpdatedAt() != null ? program.getUpdatedAt().toString() : null);
        return dto;
    }
}

package com.codearena.controller;

import com.codearena.dto.ProgramDTO;
import com.codearena.entity.User;
import com.codearena.repository.UserRepository;
import com.codearena.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProgramDTO>> getUserPrograms(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(programService.getUserPrograms(user));
    }

    @PostMapping
    public ResponseEntity<ProgramDTO> saveProgram(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        var program = programService.saveProgram(
            user,
            request.get("name"),
            request.get("language"),
            request.get("code")
        );
        return ResponseEntity.ok(programService.toDTO(program));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramDTO> getProgram(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(programService.toDTO(programService.getProgramById(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramDTO> updateProgram(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        ProgramDTO program = programService.toDTO(programService.updateProgram(
            id,
            user,
            request.get("name"),
            request.get("language"),
            request.get("code")
        ));
        return ResponseEntity.ok(program);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        programService.deleteProgram(id, user);
        return ResponseEntity.noContent().build();
    }
}


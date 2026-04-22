package com.codearena.controller;

import com.codearena.dto.ExecuteRequest;
import com.codearena.dto.ExecuteResponse;
import com.codearena.service.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/execute")
@RequiredArgsConstructor
public class ExecuteController {

    private final CodeExecutionService codeExecutionService;

    @PostMapping
    public ResponseEntity<ExecuteResponse> execute(@Valid @RequestBody ExecuteRequest request) {
        return ResponseEntity.ok(codeExecutionService.execute(request));
    }
}


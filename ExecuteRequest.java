package com.codearena.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExecuteRequest {
    @NotBlank(message = "Language is required")
    private String language;
    
    @NotBlank(message = "Code is required")
    private String code;
}

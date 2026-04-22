package com.codearena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteResponse {
    private String output;
    private String error;
    private long executionTime;
    private String status;
    private String memoryUsed;
}

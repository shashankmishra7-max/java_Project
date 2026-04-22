package com.codearena.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDTO {
    private Long id;
    private String name;
    private String language;
    private String code;
    private String createdAt;
    private String updatedAt;
}

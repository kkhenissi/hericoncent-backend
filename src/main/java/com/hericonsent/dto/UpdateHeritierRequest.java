package com.hericonsent.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateHeritierRequest {

    @Email
    private String email;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal part;
}

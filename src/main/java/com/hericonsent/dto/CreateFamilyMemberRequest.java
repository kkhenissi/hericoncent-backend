package com.hericonsent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateFamilyMemberRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private Integer birthYear;

    private Integer deathYear;

    @NotNull
    private String gender; // "male" or "female"

    private String profession;

    private String city;

    private UUID spouseId;

    private List<UUID> parentIds;

    private String photoInitials;
}

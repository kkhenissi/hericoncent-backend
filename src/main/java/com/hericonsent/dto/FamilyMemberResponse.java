package com.hericonsent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FamilyMemberResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer birthYear;
    private Integer deathYear;
    private String gender; // "male" or "female"
    private String profession;
    private String city;
    private UUID spouseId;
    private List<UUID> parentIds;
    private String photoInitials;
    private boolean validated;

    @JsonProperty("isHeir")
    private boolean isHeir;
}

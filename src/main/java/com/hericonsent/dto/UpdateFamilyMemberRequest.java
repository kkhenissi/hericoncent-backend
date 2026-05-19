package com.hericonsent.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateFamilyMemberRequest {
    private String firstName;

    private String lastName;

    private Integer birthYear;

    private Integer deathYear;

    private String gender; // "male" or "female"

    private String profession;

    private String city;

    private UUID spouseId;

    private List<UUID> parentIds;

    private String photoInitials;
}

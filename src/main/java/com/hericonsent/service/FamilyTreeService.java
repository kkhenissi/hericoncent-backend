package com.hericonsent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hericonsent.dto.CreateFamilyMemberRequest;
import com.hericonsent.dto.FamilyMemberResponse;
import com.hericonsent.dto.UpdateFamilyMemberRequest;
import com.hericonsent.entity.Personne;
import com.hericonsent.exception.ResourceNotFoundException;
import com.hericonsent.repository.PersonneRepository;
import com.hericonsent.repository.HeritierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyTreeService {

    private final PersonneRepository personneRepository;
    private final HeritierRepository heritierRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getMembersByDossier(UUID dossierId) {
        try {
            return heritierRepository.findByDossierId(dossierId)
                    .stream()
                    .map(heritier -> heritier.getPersonne())
                    .filter(personne -> personne != null)
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving family tree for dossier {}: {}", dossierId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getAllMembers() {
        return personneRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FamilyMemberResponse getMemberById(UUID id) {
        return personneRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de la famille introuvable : " + id));
    }

    @Transactional
    public FamilyMemberResponse createMember(CreateFamilyMemberRequest request) {
        Personne personne = Personne.builder()
                .nom(request.getLastName())
                .prenom(request.getFirstName())
                .gender(request.getGender())
                .birthYear(request.getBirthYear())
                .deathYear(request.getDeathYear())
                .profession(request.getProfession())
                .city(request.getCity())
                .spouseId(request.getSpouseId())
                .photoInitials(request.getPhotoInitials())
                .build();

        // Convert parentIds list to JSON string
        if (request.getParentIds() != null && !request.getParentIds().isEmpty()) {
            try {
                personne.setParentIds(objectMapper.writeValueAsString(request.getParentIds()));
            } catch (Exception e) {
                log.error("Error serializing parent IDs", e);
                personne.setParentIds("[]");
            }
        } else {
            personne.setParentIds("[]");
        }

        personne = personneRepository.save(personne);
        log.info("Nouveau membre de la famille créé : {} {}", request.getFirstName(), request.getLastName());
        return toResponse(personne);
    }

    @Transactional
    public FamilyMemberResponse updateMember(UUID id, UpdateFamilyMemberRequest request) {
        Personne personne = personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de la famille introuvable : " + id));

        if (request.getFirstName() != null) {
            personne.setPrenom(request.getFirstName());
        }
        if (request.getLastName() != null) {
            personne.setNom(request.getLastName());
        }
        if (request.getGender() != null) {
            personne.setGender(request.getGender());
        }
        if (request.getBirthYear() != null) {
            personne.setBirthYear(request.getBirthYear());
        }
        if (request.getDeathYear() != null) {
            personne.setDeathYear(request.getDeathYear());
        }
        if (request.getProfession() != null) {
            personne.setProfession(request.getProfession());
        }
        if (request.getCity() != null) {
            personne.setCity(request.getCity());
        }
        if (request.getSpouseId() != null) {
            personne.setSpouseId(request.getSpouseId());
        }
        if (request.getPhotoInitials() != null) {
            personne.setPhotoInitials(request.getPhotoInitials());
        }
        if (request.getParentIds() != null) {
            try {
                personne.setParentIds(objectMapper.writeValueAsString(request.getParentIds()));
            } catch (Exception e) {
                log.error("Error serializing parent IDs", e);
            }
        }

        personne = personneRepository.save(personne);
        log.info("Membre de la famille {} mis à jour", id);
        return toResponse(personne);
    }

    @Transactional
    public void deleteMember(UUID id) {
        Personne personne = personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membre de la famille introuvable : " + id));

        personneRepository.delete(personne);
        log.info("Membre de la famille {} supprimé", id);
    }

    @Transactional
    public void linkCouple(UUID maleId, UUID femaleId) {
        Personne male = personneRepository.findById(maleId)
                .orElseThrow(() -> new ResourceNotFoundException("Homme introuvable : " + maleId));
        Personne female = personneRepository.findById(femaleId)
                .orElseThrow(() -> new ResourceNotFoundException("Femme introuvable : " + femaleId));

        male.setSpouseId(femaleId);
        female.setSpouseId(maleId);

        personneRepository.save(male);
        personneRepository.save(female);

        log.info("Couple créé : {} <-> {}", male.getNomComplet(), female.getNomComplet());
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getChildren(UUID parentId) {
        List<Personne> children = personneRepository.findAll()
                .stream()
                .filter(p -> hasParentId(p, parentId))
                .collect(Collectors.toList());
        
        return children.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> getRoots() {
        return personneRepository.findAll()
                .stream()
                .filter(p -> p.getParentIds() == null || p.getParentIds().isEmpty() || p.getParentIds().equals("[]"))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> searchMembers(String query) {
        String lowerQuery = query.toLowerCase();
        return personneRepository.findAll()
                .stream()
                .filter(p -> 
                    p.getPrenom().toLowerCase().contains(lowerQuery) ||
                    p.getNom().toLowerCase().contains(lowerQuery) ||
                    (p.getCity() != null && p.getCity().toLowerCase().contains(lowerQuery)) ||
                    (p.getProfession() != null && p.getProfession().toLowerCase().contains(lowerQuery))
                )
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean hasParentId(Personne personne, UUID parentId) {
        if (personne.getParentIds() == null || personne.getParentIds().isEmpty()) {
            return false;
        }
        
        try {
            List<UUID> parentIds = objectMapper.readValue(
                personne.getParentIds(),
                new TypeReference<List<UUID>>() {}
            );
            return parentIds.contains(parentId);
        } catch (Exception e) {
            log.error("Error deserializing parent IDs for personne {}", personne.getId(), e);
            return false;
        }
    }

    private FamilyMemberResponse toResponse(Personne personne) {
        List<UUID> parentIds = new ArrayList<>();

        if (personne.getParentIds() != null && !personne.getParentIds().isEmpty()) {
            try {
                parentIds = objectMapper.readValue(
                    personne.getParentIds(),
                    new TypeReference<List<UUID>>() {}
                );
            } catch (Exception e) {
                log.debug("Error deserializing parent IDs for personne {}: {}", personne.getId(), e.getMessage());
            }
        }

        // Use default values if fields are null
        Integer birthYear = personne.getBirthYear() != null ? personne.getBirthYear() : 1900;
        String gender = personne.getGender() != null ? personne.getGender() : "male";

        return FamilyMemberResponse.builder()
                .id(personne.getId())
                .firstName(personne.getPrenom() != null ? personne.getPrenom() : "")
                .lastName(personne.getNom() != null ? personne.getNom() : "")
                .birthYear(birthYear)
                .deathYear(personne.getDeathYear())
                .gender(gender)
                .profession(personne.getProfession())
                .city(personne.getCity())
                .spouseId(personne.getSpouseId())
                .parentIds(parentIds)
                .photoInitials(personne.getPhotoInitials())
                .build();
    }
}

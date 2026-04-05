package com.hericonsent.service;

import com.hericonsent.dto.*;
import com.hericonsent.entity.Personne;
import com.hericonsent.entity.User;
import com.hericonsent.repository.PersonneRepository;
import com.hericonsent.repository.UserRepository;
import com.hericonsent.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PersonneRepository personneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé : " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        // Créer profil Personne associé
        Personne personne = Personne.builder()
                .user(user)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .build();
        personneRepository.save(personne);

        auditService.log("INSCRIPTION", "USER", user.getId(), user.getId(), null);
        log.info("Nouvel utilisateur inscrit : {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refresh)
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        auditService.log("CONNEXION", "USER", user.getId(), user.getId(), null);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refresh)
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        String newToken = jwtService.generateToken(user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .accessToken(newToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }
}

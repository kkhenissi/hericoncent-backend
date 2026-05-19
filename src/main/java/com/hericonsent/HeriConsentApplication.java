package com.hericonsent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HeriConsentApplication {
    public static void main(String[] args) {
        SpringApplication.run(HeriConsentApplication.class, args);
    }
    @Bean
    public UserDetailsService userDetailsService(com.hericonsent.repository.UserRepository userRepo) {
        return username -> userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));
    }
}

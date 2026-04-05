package com.hericonsent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void envoyerEmail(String destinataire, String sujet, String corps) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject("[HériConsent] " + sujet);
            message.setText(corps);
            message.setFrom("noreply@hericonsent.fr");
            mailSender.send(message);
            log.info("Email envoyé à {}", destinataire);
        } catch (Exception e) {
            log.error("Erreur envoi email à {} : {}", destinataire, e.getMessage());
        }
    }

    @Async
    public void notifierNouveauConsentement(String email, String nomHeritier,
                                             String titreDossier, String token,
                                             String baseUrl) {
        String lien = baseUrl + "/consentement/repondre?token=" + token;
        String corps = String.format("""
                Bonjour %s,

                Vous avez reçu une demande de consentement pour le dossier : "%s".

                Veuillez cliquer sur le lien ci-dessous pour consulter et répondre à cette demande :
                %s

                Ce lien est valable 72 heures.

                Si vous n'êtes pas concerné par cette demande, veuillez ignorer ce message.

                Cordialement,
                L'équipe HériConsent
                """, nomHeritier, titreDossier, lien);

        envoyerEmail(email, "Demande de consentement - " + titreDossier, corps);
    }

    @Async
    public void notifierConsentementValide(String email, String nomHeritier, String titreDossier) {
        String corps = String.format("""
                Bonjour %s,

                Bonne nouvelle ! Le consentement concernant le dossier "%s" a été validé.
                Tous les héritiers ont donné leur accord.

                Connectez-vous sur HériConsent pour consulter le détail et les prochaines étapes.

                Cordialement,
                L'équipe HériConsent
                """, nomHeritier, titreDossier);

        envoyerEmail(email, "Consentement validé - " + titreDossier, corps);
    }

    @Async
    public void notifierRelance(String email, String nomHeritier,
                                 String titreDossier, String token, String baseUrl) {
        String lien = baseUrl + "/consentement/repondre?token=" + token;
        String corps = String.format("""
                Bonjour %s,

                Rappel : votre réponse est toujours attendue pour le dossier "%s".

                Lien de réponse : %s

                Merci de répondre dès que possible.

                Cordialement,
                L'équipe HériConsent
                """, nomHeritier, titreDossier, lien);

        envoyerEmail(email, "[RAPPEL] Demande de consentement - " + titreDossier, corps);
    }
}

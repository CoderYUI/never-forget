package com.birthday.birthdayapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final HttpClient httpClient;

    public EmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Sends the Welcome Email after registration
     */
    public boolean sendWelcomeEmail(String toEmail, String username) {
        String template = loadTemplate("templates/welcome-email.html");
        if (template == null) {
            System.err.println("Failed to load welcome email template!");
            return false;
        }

        String htmlContent = template.replace("{{username}}", username);
        return sendViaResend(toEmail, "Thank you for joining us!", htmlContent);
    }

    /**
     * Sends the Birthday Email
     */
    public boolean sendBirthdayEmail(String toEmail, String username) {
        String template = loadTemplate("templates/birthday-email.html");
        if (template == null) {
            System.err.println("Failed to load birthday email template!");
            return false;
        }

        String htmlContent = template.replace("{{username}}", username);
        return sendViaResend(toEmail, "Happy Birthday! 🎂", htmlContent);
    }

    /**
     * Core method to dispatch JSON payload directly to Resend REST API (Port 443)
     */
    public boolean sendViaResend(String toEmail, String subject, String htmlContent) {
        try {
            String escapedHtml = escapeJson(htmlContent);
            String escapedSubject = escapeJson(subject);
            String escapedFrom = escapeJson(fromEmail);

            // Resend API expects: {"from": "...", "to": ["email"], "subject": "...", "html": "..."}
            String jsonPayload = String.format(
                    "{\"from\":\"%s\",\"to\":[\"%s\"],\"subject\":\"%s\",\"html\":\"%s\"}",
                    escapedFrom, toEmail, escapedSubject, escapedHtml);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Resend: Email sent successfully to " + toEmail + " -> " + response.body());
                return true;
            } else {
                System.err.println("Resend API error [" + response.statusCode() + "]: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + " via Resend: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper to load HTML template from classpath resources
     */
    private String loadTemplate(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.err.println("Error reading template file " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Minimal JSON escaping for strings
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
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

    @Value("${app.email.script-url}")
    private String scriptUrl;

    private final HttpClient httpClient;

    public EmailService() {
        // HttpClient configured to follow 302 redirects from Google Apps Script
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
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
        return sendViaAppsScript(toEmail, "Thank you for joining us!", htmlContent);
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
        return sendViaAppsScript(toEmail, "Happy Birthday! 🎂", htmlContent);
    }

    /**
     * Core method to dispatch JSON payload to Google Apps Script
     */
    public boolean sendViaAppsScript(String toEmail, String subject, String htmlContent) {
        try {
            // Escape JSON characters in HTML content
            String escapedHtml = escapeJson(htmlContent);
            String escapedSubject = escapeJson(subject);

            String jsonPayload = String.format(
                    "{\"to\":\"%s\",\"subject\":\"%s\",\"htmlBody\":\"%s\"}",
                    toEmail, escapedSubject, escapedHtml);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(scriptUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Email sent successfully to: " + toEmail);
                return true;
            } else {
                System.err.println(
                        "Google Apps Script responded with code " + response.statusCode() + ": " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
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
        if (input == null)
            return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

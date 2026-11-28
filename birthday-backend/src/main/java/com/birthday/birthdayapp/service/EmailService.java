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
import java.util.Base64;

@Service
public class EmailService {

    @Value("${mailjet.api.key}")
    private String apiKey;

    @Value("${mailjet.secret.key}")
    private String secretKey;

    @Value("${mailjet.sender.email}")
    private String senderEmail;

    @Value("${mailjet.sender.name:Birthday Wishes}")
    private String senderName;

    private final HttpClient httpClient;

    public EmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public boolean sendWelcomeEmail(String toEmail, String username) {
        String template = loadTemplate("templates/welcome-email.html");
        if (template == null) return false;

        String htmlContent = template.replace("{{username}}", username);
        return sendViaMailjet(toEmail, "Thank you for joining us!", htmlContent);
    }

    public boolean sendBirthdayEmail(String toEmail, String username) {
        String template = loadTemplate("templates/birthday-email.html");
        if (template == null) return false;

        String htmlContent = template.replace("{{username}}", username);
        return sendViaMailjet(toEmail, "Happy Birthday! 🎂", htmlContent);
    }

    public boolean sendViaMailjet(String toEmail, String subject, String htmlContent) {
        try {
            String basicAuth = Base64.getEncoder().encodeToString(
                    (apiKey.trim() + ":" + secretKey.trim()).getBytes(StandardCharsets.UTF_8)
            );

            String escapedHtml = escapeJson(htmlContent);
            String escapedSubject = escapeJson(subject);
            String escapedSenderName = escapeJson(senderName);

            String jsonPayload = String.format(
                    "{\"Messages\":[{" +
                            "\"From\":{\"Email\":\"%s\",\"Name\":\"%s\"}," +
                            "\"To\":[{\"Email\":\"%s\"}]," +
                            "\"Subject\":\"%s\"," +
                            "\"HTMLPart\":\"%s\"" +
                            "}]}",
                    senderEmail, escapedSenderName, toEmail, escapedSubject, escapedHtml
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mailjet.com/v3.1/send"))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Mailjet: Email sent successfully to " + toEmail);
                return true;
            } else {
                System.err.println("Mailjet API error [" + response.statusCode() + "]: " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Failed to send email via Mailjet: " + e.getMessage());
            return false;
        }
    }

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
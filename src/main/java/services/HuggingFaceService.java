package services;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import org.json.*;

public class HuggingFaceService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private final String apiToken;
    private final HttpClient client;

    public HuggingFaceService(String apiToken) {
        this.apiToken = apiToken;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String generateResponse(String prompt) {
        try {
            JSONObject requestBody = new JSONObject()
                    .put("model", "deepseek/deepseek-chat-v3-0324:free") // modèle compatible avec OpenRouter
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt)))
                    .put("temperature", 0.7);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseApiResponse(response.body());

        } catch (Exception e) {
            return handleException(e);
        }
    }


    private String parseApiResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (jsonResponse.has("choices")) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    return message.getString("content");
                }
            }

            if (jsonResponse.has("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                throw new RuntimeException("Erreur API: " + error.getString("message"));
            }

            throw new JSONException("Format de réponse inconnu");

        } catch (Exception e) {
            System.err.println("Réponse brute problématique: " + responseBody);
            throw new RuntimeException("Erreur de traitement: " + e.getMessage());
        }
    }


    private String handleException(Exception e) {
        System.err.println("Erreur lors de la requête: " + e.getMessage());

        if (e instanceof HttpTimeoutException) {
            return "Le service met trop de temps à répondre. Veuillez réessayer.";
        } else if (e.getCause() instanceof JSONException) {
            return "Le service a renvoyé une réponse inattendue. Contactez le support technique.";
        } else {
            return "Erreur temporaire du service. Veuillez réessayer plus tard.";
        }
    }
}
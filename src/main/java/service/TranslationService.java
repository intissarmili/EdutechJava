package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;

/**
 * Service for translating text between different languages using Google Translate API.
 */
public class TranslationService {

    // Available languages with their display names
    public static final String FRENCH = "Français";
    public static final String ENGLISH = "English";
    public static final String SPANISH = "Español";
    public static final String GERMAN = "Deutsch";
    public static final String ARABIC = "العربية";

    // Map language names to ISO codes used by Google Translate API
    private static final java.util.Map<String, String> languageCodes = new java.util.HashMap<>();

    static {
        languageCodes.put(FRENCH, "fr");
        languageCodes.put(ENGLISH, "en");
        languageCodes.put(SPANISH, "es");
        languageCodes.put(GERMAN, "de");
        languageCodes.put(ARABIC, "ar");
    }

    /**
     * Translates text from French to the target language using Google Translate API.
     * This uses the unofficial Google Translate API which does not require an API key.
     *
     * @param text The text to translate
     * @param targetLanguage The language to translate to
     * @return The translated text
     */
    public String translate(String text, String targetLanguage) {
        try {
            // Default source language is French
            String sourceLanguage = "fr";

            // Get the target language code
            String targetLangCode = languageCodes.get(targetLanguage);

            // If we're translating to French or target language code is not found, return the original text
            if (targetLangCode == null || targetLangCode.equals(sourceLanguage)) {
                return text;
            }

            // Use the unofficial Google Translate API
            String urlStr = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=" + sourceLanguage +
                    "&tl=" + targetLangCode +
                    "&dt=t" +
                    "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Read the response
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse the JSON response
            String jsonResponse = response.toString();
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // The response structure is: [[["translated text","original text",null,null,1]],null,"fr"]
            // We need to extract the first element which is another array
            StringBuilder translatedText = new StringBuilder();

            JSONArray translationsArray = jsonArray.getJSONArray(0);
            for (int i = 0; i < translationsArray.length(); i++) {
                JSONArray translationLine = translationsArray.getJSONArray(i);
                translatedText.append(translationLine.getString(0));
            }

            return translatedText.toString();

        } catch (Exception e) {
            e.printStackTrace();
            // In case of error, return original text with error message
            System.err.println("Translation error: " + e.getMessage());
            return text; // Return original text on error for better user experience
        }
    }

    /**
     * Returns an array of available languages for translation.
     *
     * @return An array of language names
     */
    public String[] getAvailableLanguages() {
        return new String[] {FRENCH, ENGLISH, SPANISH, GERMAN, ARABIC};
    }

    /**
     * Detects the language of the given text.
     * In this implementation, we assume the source language is French.
     *
     * @param text The text to analyze
     * @return The detected language or FRENCH as default
     */
    public String detectLanguage(String text) {
        // In a full implementation, you would use the Google Translate API to detect language
        // For simplicity, we'll assume the source language is French
        return FRENCH;
    }
}
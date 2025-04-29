package models;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class to represent a comment with translations to different languages.
 */
public class TranslatedComment {
    private Commentaire originalComment;
    private Map<String, String> translations;
    private String currentLanguage;
    
    public TranslatedComment(Commentaire comment) {
        this.originalComment = comment;
        this.translations = new HashMap<>();
        this.currentLanguage = "Français"; // Default language
    }
    
    /**
     * Get the original comment.
     * 
     * @return The original comment object
     */
    public Commentaire getOriginalComment() {
        return originalComment;
    }
    
    /**
     * Add a translation for the comment.
     * 
     * @param language The language of the translation
     * @param translatedText The translated text
     */
    public void addTranslation(String language, String translatedText) {
        translations.put(language, translatedText);
    }
    
    /**
     * Get the translation for a specific language.
     * 
     * @param language The language to get the translation for
     * @return The translated text, or the original if not available
     */
    public String getTranslation(String language) {
        if (translations.containsKey(language)) {
            return translations.get(language);
        }
        return originalComment.getContenu(); // Return original if translation not available
    }
    
    /**
     * Get the current language for display.
     * 
     * @return The current language
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Set the current language for display.
     * 
     * @param language The language to set
     */
    public void setCurrentLanguage(String language) {
        this.currentLanguage = language;
    }
    
    /**
     * Get the comment content in the current language.
     * 
     * @return The comment text in the current language
     */
    public String getCurrentText() {
        return getTranslation(currentLanguage);
    }
    
    /**
     * Check if a translation is available for a language.
     * 
     * @param language The language to check
     * @return True if a translation exists, false otherwise
     */
    public boolean hasTranslation(String language) {
        return translations.containsKey(language);
    }
    
    /**
     * Get all available translations.
     * 
     * @return Map of all translations
     */
    public Map<String, String> getAllTranslations() {
        return translations;
    }
} 
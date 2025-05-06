package utils;

import java.util.Arrays;
import java.util.List;

public class CommentValidator {
    private static final List<String> FORBIDDEN_WORDS = Arrays.asList(
        "insulte", "gros mot", "hate", "racisme", "violence", "merde", "putain", 
        "connard", "connasse", "batard", "batarde", "salope", "pute", "merdique",
        "conneries", "connerie", "con", "merdique", "merdiques", "merdique", "merdiques",
        "connard", "connards", "connasse", "connasses", "batard", "batards", "batarde", "batardes",
        "salope", "salopes", "pute", "putes", "merdique", "merdiques", "merdique", "merdiques"
    );

    public static boolean containsForbiddenContent(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        String lowerComment = comment.toLowerCase();
        
        // Log pour le débogage
        System.out.println("Vérification du commentaire: " + comment);
        
        for (String word : FORBIDDEN_WORDS) {
            if (lowerComment.contains(word.toLowerCase())) {
                System.out.println("Mot interdit détecté: " + word);
                return true;
            }
        }
        
        return false;
    }
} 
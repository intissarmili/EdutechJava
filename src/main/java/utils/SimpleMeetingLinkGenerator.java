package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * A simple utility class to generate meeting links without external dependencies.
 * Uses predefined meeting services like Jitsi Meet which don't require authentication.
 */
public class SimpleMeetingLinkGenerator {
    
    // Jitsi Meet is a free, open-source video conferencing solution
    private static final String JITSI_MEET_BASE_URL = "https://meet.jit.si/";
    
    // Alternative services
    private static final String WHEREBY_BASE_URL = "https://whereby.com/";
    
    /**
     * Generate a unique meeting URL using Jitsi Meet
     * @param topic The topic of the meeting (will be sanitized for URL)
     * @return A URL that can be used to start a meeting immediately
     */
    public static String generateJitsiMeetLink(String topic) {
        // Create a unique room name based on topic and date
        String sanitizedTopic = sanitizeForUrl(topic);
        String uniqueId = generateUniqueId();
        
        // Combine to create meeting URL
        return JITSI_MEET_BASE_URL + "TutoringSession-" + sanitizedTopic + "-" + uniqueId;
    }
    
    /**
     * Generate a unique meeting URL using Whereby (formerly appear.in)
     * Note: Free Whereby rooms have limitations on number of participants
     * @param topic The topic of the meeting
     * @return A URL for a Whereby meeting room
     */
    public static String generateWherebyLink(String topic) {
        // Create a unique room name
        String sanitizedTopic = sanitizeForUrl(topic);
        String uniqueId = generateUniqueId();
        
        return WHEREBY_BASE_URL + "tutoring-" + sanitizedTopic + "-" + uniqueId;
    }
    
    /**
     * Generate a meeting URL using the default provider (Jitsi Meet)
     * @param topic Meeting topic
     * @return A ready-to-use meeting URL
     */
    public static String generateMeetingLink(String topic) {
        return generateJitsiMeetLink(topic);
    }
    
    /**
     * Sanitize the topic to be used in a URL
     * Removes special characters and spaces
     */
    private static String sanitizeForUrl(String input) {
        if (input == null || input.isEmpty()) {
            return "meeting";
        }
        
        // Replace spaces with hyphens and remove special characters
        return input.trim()
                .toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .substring(0, Math.min(input.length(), 20)); // Limit length
    }
    
    /**
     * Generate a short unique ID for the meeting
     */
    private static String generateUniqueId() {
        // Current date/time component
        LocalDateTime now = LocalDateTime.now();
        String dateComponent = now.format(DateTimeFormatter.ofPattern("MMddHHmm"));
        
        // Random component (3 digits)
        Random random = new Random();
        String randomComponent = String.format("%03d", random.nextInt(1000));
        
        return dateComponent + randomComponent;
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("Jitsi Meet link: " + generateJitsiMeetLink("Math Tutoring"));
        System.out.println("Whereby link: " + generateWherebyLink("Math Tutoring"));
    }
} 
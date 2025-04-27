package utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Text-to-speech implementation using eSpeak.
 * Requires eSpeak to be installed on the system.
 */
public class ESpeakTTS {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static boolean available = false;
    private static String espeakPath = "espeak"; // Default command
    
    // Voice configuration
    private static String voice = "fr-fr";  // French voice
    private static int speed = 145;        // Speed (words per minute) - slightly slower for better clarity
    private static int pitch = 50;         // Pitch (0-99)
    private static int amplitude = 100;    // Volume

    static {
        // Check if eSpeak is available
        try {
            // First log detection attempt
            System.out.println("Searching for eSpeak installation...");
            
            // On Windows, try exact paths first
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Common installation paths - starting with known location
                String[] possiblePaths = {
                    "C:\\Program Files (x86)\\eSpeak\\command_line\\espeak.exe",
                    "C:\\Program Files (x86)\\eSpeak\\espeak.exe",
                    "C:\\Program Files\\eSpeak\\command_line\\espeak.exe",
                    "C:\\Program Files\\eSpeak\\espeak.exe",
                    System.getenv("ProgramFiles") + "\\eSpeak\\command_line\\espeak.exe",
                    System.getenv("ProgramFiles(x86)") + "\\eSpeak\\command_line\\espeak.exe"
                };
                
                for (String path : possiblePaths) {
                    System.out.println("Checking path: " + path);
                    File file = new File(path);
                    if (file.exists()) {
                        espeakPath = path;
                        available = true;
                        System.out.println("Found eSpeak at: " + espeakPath);
                        break;
                    }
                }
            }
            
            // Try the default command if not found yet
            if (!available) {
                try {
                    Process process = Runtime.getRuntime().exec(new String[]{espeakPath, "--version"});
                    int exitCode = process.waitFor();
                    available = (exitCode == 0);
                } catch (Exception e) {
                    System.out.println("Default command failed: " + e.getMessage());
                }
            }
            
            if (available) {
                System.out.println("eSpeak found at: " + espeakPath);
            } else {
                System.out.println("eSpeak not found. Install it from http://espeak.sourceforge.net/");
            }
        } catch (Exception e) {
            System.out.println("eSpeak not available: " + e.getMessage());
        }
    }
    
    /**
     * Speak the given text using eSpeak
     * @param text Text to speak
     * @return true if successful
     */
    public static boolean speak(String text) {
        if (!available) {
            // Show an alert if eSpeak is not available
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Text-to-Speech Unavailable");
                alert.setHeaderText("eSpeak not found");
                alert.setContentText("Please install eSpeak from http://espeak.sourceforge.net/ to enable text-to-speech functionality.");
                alert.showAndWait();
            });
            return false;
        }
        
        // Run in background thread to not block UI
        executor.submit(() -> {
            try {
                // Escape quotes and special characters
                String safeText = text.replace("\"", "").replace("'", "");
                
                System.out.println("Speaking text using eSpeak at: " + espeakPath);
                
                // Build eSpeak command based on OS
                ProcessBuilder pb;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    // Windows needs special handling
                    // Check for quotes in the path
                    String path = espeakPath;
                    if (path.contains(" ") && !path.startsWith("\"")) {
                        path = "\"" + path + "\"";
                    }
                    
                    // Enhanced parameters for better voice quality
                    String cmd = path + " -v " + voice + " -s " + speed + " -a " + amplitude + " -p " + pitch + " -g 10 \"" + safeText + "\"";
                    System.out.println("Executing: " + cmd);
                    
                    // Execute command directly
                    Process process = Runtime.getRuntime().exec(cmd);
                    process.waitFor();
                } else {
                    // Linux and Mac
                    pb = new ProcessBuilder(
                        "espeak", 
                        "-v", voice,
                        "-s", String.valueOf(speed),
                        "-a", String.valueOf(amplitude),
                        "-p", String.valueOf(pitch),
                        "-g", "10", // Word gap in 10ms units
                        "\"" + safeText + "\""
                    );
                    
                    // Start process and wait for completion
                    Process process = pb.start();
                    process.waitFor();
                }
                
            } catch (IOException | InterruptedException e) {
                System.err.println("Error using eSpeak: " + e.getMessage());
                e.printStackTrace(); // Add stack trace for debugging
                
                // Show error alert
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Text-to-Speech Error");
                    alert.setHeaderText("Error during speech synthesis");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        });
        
        return true;
    }
    
    /**
     * Set the voice parameters
     * @param voice Voice identifier (e.g., "en", "fr-fr")
     * @param speed Words per minute (default 145)
     * @param pitch Voice pitch (0-99)
     */
    public static void setVoiceParams(String voice, int speed, int pitch, int amplitude) {
        ESpeakTTS.voice = voice;
        ESpeakTTS.speed = speed;
        ESpeakTTS.pitch = pitch;
        ESpeakTTS.amplitude = amplitude;
    }
    
    /**
     * Test if eSpeak is available
     */
    public static boolean isAvailable() {
        return available;
    }
    
    /**
     * Test method
     */
    public static void main(String[] args) {
        speak("Ceci est un test du système de synthèse vocale eSpeak");
        // These settings produce slightly less robotic results
        setVoiceParams("fr-fr", 130, 45, 90);
    }
} 
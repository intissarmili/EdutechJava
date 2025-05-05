package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.ESpeakTTS;
import utils.GoogleMeetService;
import utils.EmailService;
import utils.ReservationExample;

public class Home extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {



            // Load the FXML file (you can change the path if needed)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat_view.fxml"));
            Parent root = loader.load();

            // Set up the stage
            primaryStage.setTitle("Tutor Availability System");
            primaryStage.setScene(new Scene(root, 1024, 768));

            // Prevent window from being resized too small
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

          

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load the FXML file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Initialize services before launching JavaFX application
        initializeServices();
        launch(args);
    }

    /**
     * Initialize all services used by the application
     */
    private static void initializeServices() {
        // Initialize TTS
        try {
            System.out.println("Initializing TTS system...");
            boolean ttsAvailable = ESpeakTTS.isAvailable();
            System.out.println("TTS available: " + ttsAvailable);

            if (!ttsAvailable) {
                System.err.println("WARNING: eSpeak not found, text-to-speech will not be available.");
                System.err.println("Install eSpeak from http://espeak.sourceforge.net/");
            } else {
                ESpeakTTS.setVoiceParams("fr-fr", 130, 45, 90);
            }
        } catch (Exception e) {
            System.err.println("Error during TTS initialization:");
            e.printStackTrace();
        }

        // Initialize Email service
        try {
            System.out.println("Initializing Email service...");
            EmailService.initialize();
            System.out.println("Email Service available: " + EmailService.isAvailable());

            if (EmailService.isAvailable()) {
                System.out.println("Attempting to send a test email...");
                boolean testResult = EmailService.sendTestEmail();
                System.out.println("Test email result: " + (testResult ? "Success" : "Failed"));
            }
        } catch (Exception e) {
            System.err.println("Error during Email service initialization:");
            e.printStackTrace();
        }

        // Initialize Google Meet service
        try {
            System.out.println("Initializing Google Meet service...");
            GoogleMeetService.initialize();
            System.out.println("Google Meet service available: " + GoogleMeetService.isAvailable());
        } catch (Exception e) {
            System.err.println("Error during Google Meet service initialization:");
            e.printStackTrace();
        }
    }
}

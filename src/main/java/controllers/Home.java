package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import utils.ESpeakTTS;
import utils.GoogleMeetService;
import utils.EmailService;
import utils.ReservationExample;

public class Home extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/avaibility/listCards.fxml"));
            Parent root = loader.load();

            // Set up the stage
            primaryStage.setTitle("Tutor Availability System");
            primaryStage.setScene(new Scene(root, 1024, 768));

            // Prevent window from being resized too small
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Add test button for email
            if (root instanceof VBox) {
                Button testEmailButton = new Button("Test Reservation Email");
                testEmailButton.setOnAction(e -> ReservationExample.sendTestReservationEmail());
                ((VBox) root).getChildren().add(testEmailButton);
            }

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // You might want to show an alert to the user here
            System.err.println("Failed to load the FXML file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Initialize services
        initializeServices();
        
        // Launch JavaFX application
        launch(args);
    }
    
    /**
     * Initialize all services used by the application
     */
    private static void initializeServices() {
        // Initialize TTS
        try {
            System.out.println("Initializing TTS system...");
            // Check if eSpeak is available
            boolean ttsAvailable = ESpeakTTS.isAvailable();
            System.out.println("TTS available: " + ttsAvailable);
            
            if (!ttsAvailable) {
                System.err.println("WARNING: eSpeak not found, text-to-speech will not be available.");
                System.err.println("Install eSpeak from http://espeak.sourceforge.net/");
            } else {
                // Set better voice parameters
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
            
            // Try to send a test email
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

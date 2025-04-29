package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Example class for demonstrating reservation confirmation emails
 */
public class ReservationExample {
    
    /**
     * Send a test reservation confirmation email
     */
    public static void sendTestReservationEmail() {
        try {
            // Make sure email service is initialized
            if (!EmailService.isAvailable()) {
                showInitializationAlert();
                return;
            }
            
            // Show confirmation dialog
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Send Test Reservation Email");
            confirmAlert.setHeaderText("Send a test reservation confirmation");
            confirmAlert.setContentText("Do you want to send a test reservation confirmation email?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Get current date and time
                    LocalDate today = LocalDate.now();
                    LocalTime startTime = LocalTime.of(14, 0); // 2:00 PM
                    LocalTime endTime = LocalTime.of(15, 0);   // 3:00 PM
                    
                    // Format date and time for email
                    String date = today.toString();
                    String time = startTime + " - " + endTime;
                    
                    // Send confirmation email with static data
                    boolean sent = EmailService.sendReservationConfirmation(
                        "Student Name", 
                        "Tutor Name", 
                        date, 
                        time, 
                        "Programming Assistance"
                    );
                    
                    // Show result
                    if (sent) {
                        showAlert("Email Sent", 
                            "A test reservation confirmation email has been sent.",
                            AlertType.INFORMATION);
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error sending test reservation email: " + e.getMessage());
            e.printStackTrace();
            
            showAlert("Email Error", 
                "Failed to send test reservation email: " + e.getMessage(),
                AlertType.ERROR);
        }
    }
    
    /**
     * Show initialization alert
     */
    private static void showInitializationAlert() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Email Service Not Available");
        alert.setHeaderText("Email Service Not Initialized");
        alert.setContentText("The email service is not available. Would you like to initialize it now?");
        
        ButtonType buttonInitialize = new ButtonType("Initialize");
        ButtonType buttonCancel = ButtonType.CANCEL;
        
        alert.getButtonTypes().setAll(buttonInitialize, buttonCancel);
        
        alert.showAndWait().ifPresent(type -> {
            if (type == buttonInitialize) {
                EmailService.initialize();
                
                // Try again if initialization succeeded
                if (EmailService.isAvailable()) {
                    Platform.runLater(() -> sendTestReservationEmail());
                }
            }
        });
    }
    
    /**
     * Show an alert dialog
     * @param title The alert title
     * @param content The alert content
     * @param type The alert type
     */
    private static void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
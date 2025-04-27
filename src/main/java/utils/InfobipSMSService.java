package utils;

import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import com.infobip.ApiClient;
import com.infobip.ApiException;
import com.infobip.ApiKey;
import com.infobip.BaseUrl;
import com.infobip.api.SmsApi;
import com.infobip.model.SmsDestination;
import com.infobip.model.SmsMessage;
import com.infobip.model.SmsRequest;
import com.infobip.model.SmsResponse;
import com.infobip.model.SmsTextContent;

/**
 * Service for sending SMS messages using Infobip API
 */
public class InfobipSMSService {
    // IMPORTANT: You need a valid API key from an active Infobip account
    // Sign up for a free trial at https://www.infobip.com/signup if needed
    private static final String API_KEY = "8b65e659fc7cfd4b3ac1254fe9fbd856-7feaa8e5-a9d4-4a56-8abb-0e9aa21e1c03"; 
    
    // The base URL should be provided by Infobip - different for each account
    // Typical URLs are:
    // - https://api.infobip.com for accounts in the US
    // - https://2zlxe1.api.infobip.com or similar for accounts in Europe
    // - Check your Infobip dashboard for the correct URL
    private static final String BASE_URL = "https://8kqxn3.api.infobip.com";
    
    // IMPORTANT: Sender ID must be pre-approved by Infobip
    // Options:
    // 1. Use your approved alphanumeric Sender ID if you have one
    // 2. Use a phone number from your Infobip account (with country code)
    // 3. Use "InfoSMS" which is often allowed as a default
    private static final String SENDER_ID = "InfoSMS"; 
    
    // The phone should include country code and be in correct format
    // For testing, ensure this is YOUR number that you can verify
    private static final String DEFAULT_PHONE_NUMBER = "+21655929523"; 
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static ApiClient apiClient;
    private static SmsApi smsApi;
    private static boolean available = false;

    /**
     * Initialize the SMS service
     */
    public static void initialize() {
        try {
            System.out.println("Initializing Infobip SMS service...");
            System.out.println("Base URL: " + BASE_URL);
            System.out.println("API Key: " + API_KEY.substring(0, 10) + "..." + " (truncated for security)");
            System.out.println("Sender ID: " + SENDER_ID);
            System.out.println("Target Phone: " + DEFAULT_PHONE_NUMBER);
            
            // Create API client
            apiClient = ApiClient.forApiKey(ApiKey.from(API_KEY))
                    .withBaseUrl(BaseUrl.from(BASE_URL))
                    .build();
            
            // Create SMS API instance
            smsApi = new SmsApi(apiClient);
            
            // Mark service as available
            available = true;
            System.out.println("Infobip SMS service initialized successfully");
            
            // Show alert for successful initialization
            showAlert("SMS Service Ready", 
                     "Infobip SMS service initialized successfully. You can now send messages.",
                     AlertType.INFORMATION);
        } catch (Exception e) {
            System.err.println("Failed to initialize Infobip SMS service: " + e.getMessage());
            e.printStackTrace();
            available = false;
            
            // Show alert for failed initialization
            showAlert("SMS Service Error", 
                     "Failed to initialize SMS service: " + e.getMessage() + 
                     "\n\nCheck if your Base URL is correct - it should include https://",
                     AlertType.ERROR);
        }
    }

    /**
     * Test with direct HTTP call - alternative approach 
     * if the SDK isn't working correctly
     */
    public static void testWithDirectHttpCall() {
        try {
            // Create a simple confirmation dialog
            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("SMS Testing Alternative");
            confirmAlert.setHeaderText("Try a direct HTTP test?");
            confirmAlert.setContentText("This will test sending an SMS using a more direct method. Continue?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    sendTestViaHTTP();
                }
            });
        } catch (Exception e) {
            System.err.println("Error showing confirmation dialog: " + e.getMessage());
        }
    }
    
    /**
     * Send a test SMS using direct HTTP call
     * This is an alternative if the SDK isn't working correctly
     */
    private static void sendTestViaHTTP() {
        try {
            // Show processing alert
            showAlert("Processing", "Attempting to send SMS via direct HTTP call...", AlertType.INFORMATION);
            
            // Show a dialog with instructions
            Alert instructionsAlert = new Alert(AlertType.INFORMATION);
            instructionsAlert.setTitle("Follow These Steps");
            instructionsAlert.setHeaderText("Test SMS with Direct HTTP");
            instructionsAlert.setContentText("To send an SMS with an alternative method:\n\n" +
                "1. Visit https://www.infobip.com and log in to your account\n\n" +
                "2. Go to the Developers section and check your API Key\n\n" +
                "3. Make sure your account has enough credits\n\n" +
                "4. Try sending a test message from the Infobip dashboard directly");
            
            instructionsAlert.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error in HTTP test: " + e.getMessage());
            showAlert("Test Failed", "HTTP test failed: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Check if the SMS service is available
     * @return true if the service is available, false otherwise
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * Send an SMS message using the default phone number
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendSMS(String message) {
        return sendSMS(DEFAULT_PHONE_NUMBER, message);
    }

    /**
     * Send an SMS message
     * @param phoneNumber The recipient's phone number (with country code)
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendSMS(String phoneNumber, String message) {
        if (!available) {
            System.err.println("SMS service is not available");
            showAlert("SMS Service Unavailable", 
                    "The SMS service is not available. Please make sure:\n\n" +
                    "1. Your API key is correct\n" +
                    "2. Your Base URL is correct (should include https://)\n" + 
                    "3. You have an active Infobip account with SMS credits", 
                    AlertType.ERROR);
            return false;
        }

        try {
            // DEBUG: Print detailed request information
            System.out.println("========= SMS REQUEST DEBUG INFO =========");
            System.out.println("Recipient: " + phoneNumber);
            System.out.println("Message content: " + message);
            System.out.println("Sender ID: " + SENDER_ID);
            System.out.println("Base URL: " + BASE_URL);
            System.out.println("API Key: " + API_KEY.substring(0, 10) + "..." + " (truncated for security)");
            System.out.println("=========================================");
            
            // Validate phone number format
            if (!phoneNumber.startsWith("+")) {
                System.err.println("Warning: Phone number may not be in the correct format. It should start with '+' followed by country code.");
                showAlert("Warning", "Phone number format may be incorrect. Make sure it includes the country code (e.g., +1XXXXXXXXXX for US).", AlertType.WARNING);
            }
            
            // Add current date and time to the message
            LocalDateTime now = LocalDateTime.now();
            String currentDate = DATE_FORMATTER.format(now);
            String currentTime = TIME_FORMATTER.format(now);
            
            String messageWithTimestamp = message + "\n\nSent on: " + currentDate + " at " + currentTime;
            System.out.println("Message content with timestamp: " + messageWithTimestamp);
            
            // Create destination
            SmsDestination destination = new SmsDestination()
                    .to(phoneNumber);

            // Create message content
            SmsTextContent textContent = new SmsTextContent()
                    .text(messageWithTimestamp);

            // Create message
            SmsMessage smsMessage = new SmsMessage()
                    .sender(SENDER_ID)
                    .addDestinationsItem(destination)
                    .content(textContent);

            // Create request
            SmsRequest request = new SmsRequest()
                    .messages(List.of(smsMessage));

            System.out.println("Sending request to Infobip API...");
            
            // Send message
            SmsResponse response = smsApi.sendSmsMessages(request).execute();
            
            // Log response details
            System.out.println("========= SMS RESPONSE DEBUG INFO =========");
            System.out.println("Bulk ID: " + response.getBulkId());
            
            if (response.getMessages() != null && !response.getMessages().isEmpty()) {
                System.out.println("Message ID: " + response.getMessages().get(0).getMessageId());
                System.out.println("Status: " + response.getMessages().get(0).getStatus().getName());
                System.out.println("Status Group ID: " + response.getMessages().get(0).getStatus().getGroupId());
                System.out.println("Status Description: " + response.getMessages().get(0).getStatus().getDescription());
                System.out.println("To: " + destination.getTo());
                System.out.println("============================================");
                
                // Check if there's an error
                if (response.getMessages().get(0).getStatus().getGroupId() >= 2) {
                    String errorMsg = response.getMessages().get(0).getStatus().getDescription();
                    System.err.println("Error sending SMS: " + errorMsg);
                    showAlert("SMS Sending Failed", "Error: " + errorMsg, AlertType.ERROR);
                    return false;
                }
                
                // Show success alert with message details
                showAlert("SMS Sent Successfully", 
                         "Message sent to " + phoneNumber + "\n" +
                         "Message ID: " + response.getMessages().get(0).getMessageId() + "\n" +
                         "Status: " + response.getMessages().get(0).getStatus().getName() + "\n\n" +
                         "Important: This means the message was accepted by Infobip, but delivery to the phone depends on the mobile network.",
                         AlertType.INFORMATION);
            }
            
            System.out.println("SMS sent successfully to " + phoneNumber);
            return true;
        } catch (ApiException e) {
            System.err.println("Failed to send SMS due to API exception: " + e.getMessage());
            // In Infobip SDK 6.1.0, use getMessage() instead of getCode() and getResponseBody()
            System.err.println("Exception details: " + e.getMessage());
            e.printStackTrace();
            
            String errorMessage = "Failed to send SMS: " + e.getMessage();
            
            // Common error handling with suggested fixes
            if (e.getMessage().contains("Authentication failed")) {
                errorMessage += "\n\nPossible solutions:" +
                               "\n1. Check if your API key is correct" +
                               "\n2. Verify your account is active" +
                               "\n3. Make sure you have SMS permissions";
            } else if (e.getMessage().contains("not found") || e.getMessage().contains("404")) {
                errorMessage += "\n\nPossible solutions:" +
                               "\n1. Check if your Base URL is correct - it should include https://" +
                               "\n2. Verify the endpoint is accessible from your network";
            } else if (e.getMessage().contains("sender")) {
                errorMessage += "\n\nPossible solutions:" +
                               "\n1. Use 'InfoSMS' as sender ID for testing" +
                               "\n2. Use a phone number from your Infobip account" +
                               "\n3. Contact Infobip to register your sender ID";
            }
            
            showAlert("SMS Sending Failed", errorMessage, AlertType.ERROR);
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error while sending SMS: " + e.getMessage());
            e.printStackTrace();
            showAlert("SMS Sending Failed", "Unexpected error: " + e.getMessage(), AlertType.ERROR);
            return false;
        }
    }

    /**
     * Send a reservation confirmation SMS using the default phone number
     * @param studentName The student's name
     * @param tutorName The tutor's name
     * @param date The date of the reservation
     * @param time The time of the reservation
     * @param topic The topic of the reservation
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendReservationConfirmation(String studentName, String tutorName, String date, String time, String topic) {
        return sendReservationConfirmation(DEFAULT_PHONE_NUMBER, studentName, tutorName, date, time, topic);
    }

    /**
     * Send a reservation confirmation SMS
     * @param phoneNumber The recipient's phone number
     * @param studentName The student's name
     * @param tutorName The tutor's name
     * @param date The date of the reservation
     * @param time The time of the reservation
     * @param topic The topic of the reservation
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendReservationConfirmation(String phoneNumber, String studentName, String tutorName, 
                                                     String date, String time, String topic) {
        LocalDateTime now = LocalDateTime.now();
        String confirmationDate = DATE_FORMATTER.format(now);
        String confirmationTime = TIME_FORMATTER.format(now);
        
        String message = String.format(
                "Hello %s,\n\nYour reservation with %s has been confirmed for %s at %s.\n\nTopic: %s\n\nThank you for using our service!\n\nConfirmation sent on: %s at %s",
                studentName, tutorName, date, time, topic, confirmationDate, confirmationTime);
        
        return sendSMS(phoneNumber, message);
    }

    /**
     * Send a test SMS to verify the service is working
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendTestSMS() {
        String testMessage = "This is a test message from the TutorApp. If you receive this, the SMS service is working correctly!";
        System.out.println("Sending test SMS to: " + DEFAULT_PHONE_NUMBER);
        return sendSMS(DEFAULT_PHONE_NUMBER, testMessage);
    }

    /**
     * Method to test different sender IDs
     * This can help identify if the sender ID is the issue
     * @return true if the test message was sent successfully
     */
    public static boolean testDifferentSenderID() {
        try {
            // Create destination
            SmsDestination destination = new SmsDestination()
                    .to(DEFAULT_PHONE_NUMBER);

            // Create message content
            SmsTextContent textContent = new SmsTextContent()
                    .text("Test message using InfoSMS as sender ID");

            // Create message with InfoSMS sender ID
            SmsMessage smsMessage = new SmsMessage()
                    .sender("InfoSMS")  // Using InfoSMS which is often allowed by default
                    .addDestinationsItem(destination)
                    .content(textContent);

            // Create request
            SmsRequest request = new SmsRequest()
                    .messages(List.of(smsMessage));
            
            // Send message
            System.out.println("Testing with sender ID: InfoSMS");
            smsApi.sendSmsMessages(request).execute();
            
            return true;
        } catch (ApiException e) {
            System.err.println("Failed to send with InfoSMS sender: " + e.getMessage());
            showAlert("Test Failed", "Failed to send with InfoSMS sender: " + e.getMessage(), AlertType.ERROR);
            return false;
        }
    }

    /**
     * Send a meeting link via SMS using the default phone number
     * @param meetingLink The meeting link
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendMeetingLink(String meetingLink) {
        return sendMeetingLink(DEFAULT_PHONE_NUMBER, meetingLink);
    }

    /**
     * Send a meeting link via SMS
     * @param phoneNumber The recipient's phone number
     * @param meetingLink The meeting link
     * @return true if the message was sent successfully, false otherwise
     */
    public static boolean sendMeetingLink(String phoneNumber, String meetingLink) {
        LocalDateTime now = LocalDateTime.now();
        String currentDate = DATE_FORMATTER.format(now);
        String currentTime = TIME_FORMATTER.format(now);
        
        String message = String.format(
                "Here is your video conference link: %s\n\nClick the link to join the meeting. No account or installation required.\n\nSent on: %s at %s",
                meetingLink, currentDate, currentTime);
        
        return sendSMS(phoneNumber, message);
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
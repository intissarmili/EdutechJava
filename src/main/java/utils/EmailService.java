package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Service for sending emails using JavaMail API
 */
public class EmailService {
    private static final String EMAIL_HOST = "smtp.gmail.com";
    private static final String EMAIL_PORT = "587";
    private static final String EMAIL_USERNAME = "intissarmili1003@gmail.com"; // Replace with your email
    private static final String EMAIL_PASSWORD = "uaoc zzts cxjd tikz";   // Replace with your app password
    private static final String EMAIL_FROM = "intissarmili1003@gmail.com"; // Replace with your email
    private static final String DEFAULT_RECIPIENT = "ahmedboutrif0@gmail.com"; // Replace with default recipient

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static Session session;
    private static boolean available = false;

    /**
     * Initialize the email service
     */
    public static void initialize() {
        try {
            System.out.println("Initializing Email service...");

            // Set mail properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", EMAIL_HOST);
            properties.put("mail.smtp.port", EMAIL_PORT);
            properties.put("mail.smtp.ssl.trust", EMAIL_HOST);
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Create session with authenticator
            session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            // Enable debug mode to see what's happening with the connection
            session.setDebug(true);

            // Mark service as available
            available = true;
            System.out.println("Email service initialized successfully");

            // Show alert for successful initialization
            showAlert("Email Service Ready",
                    "Email service initialized successfully. You can now send emails.",
                    AlertType.INFORMATION);
        } catch (Exception e) {
            System.err.println("Failed to initialize Email service: " + e.getMessage());
            e.printStackTrace();
            available = false;

            // Show alert for failed initialization
            showAlert("Email Service Error",
                    "Failed to initialize Email service: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    /**
     * Check if the email service is available
     * @return true if the service is available, false otherwise
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * Send an email using the default recipient
     * @param subject The email subject
     * @param message The email message
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendEmail(String subject, String message) {
        return sendEmail(DEFAULT_RECIPIENT, subject, message);
    }

    /**
     * Send an email
     * @param recipient The recipient's email address
     * @param subject The email subject
     * @param message The email message
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendEmail(String recipient, String subject, String message) {
        if (!available) {
            System.err.println("Email service is not available");
            showAlert("Email Service Unavailable",
                    "The email service is not available. Please check your configuration.",
                    AlertType.ERROR);
            return false;
        }

        try {
            // Add current date and time to the message
            LocalDateTime now = LocalDateTime.now();
            String currentDate = DATE_FORMATTER.format(now);
            String currentTime = TIME_FORMATTER.format(now);

            String messageWithTimestamp = message + "\n\nSent on: " + currentDate + " at " + currentTime;

            // Create a MimeMessage
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(EMAIL_FROM));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(messageWithTimestamp);

            // Send the message
            Transport.send(mimeMessage);

            System.out.println("Email sent successfully to " + recipient);

            // Show success alert
            showAlert("Email Sent Successfully",
                    "Email sent to " + recipient,
                    AlertType.INFORMATION);

            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();

            showAlert("Email Sending Failed",
                    "Failed to send email: " + e.getMessage(),
                    AlertType.ERROR);
            return false;
        }
    }

    /**
     * Send a reservation confirmation email
     * @param recipientEmail The recipient's email address
     * @param studentName The student's name
     * @param tutorName The tutor's name
     * @param date The date of the reservation
     * @param time The time of the reservation
     * @param topic The topic of the reservation
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendReservationConfirmation(String recipientEmail, String studentName, String tutorName,
                                                      String date, String time, String topic) {
        String subject = "Reservation Confirmation";

        String message = String.format(
                "Hello %s,\n\n" +
                        "Your reservation with %s has been confirmed for %s at %s.\n\n" +
                        "Topic: %s\n\n" +
                        "Thank you for using our tutoring service!\n\n" +
                        "Best regards,\n" +
                        "TutorApp Team",
                studentName, tutorName, date, time, topic);

        return sendEmail(recipientEmail, subject, message);
    }

    /**
     * Send a reservation confirmation email using the default recipient
     * @param studentName The student's name
     * @param tutorName The tutor's name
     * @param date The date of the reservation
     * @param time The time of the reservation
     * @param topic The topic of the reservation
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendReservationConfirmation(String studentName, String tutorName,
                                                      String date, String time, String topic) {
        return sendReservationConfirmation(DEFAULT_RECIPIENT, studentName, tutorName, date, time, topic);
    }

    /**
     * Send a test email to verify the service is working
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendTestEmail() {
        String subject = "Test Email from TutorApp";
        String testMessage = "This is a test message from the TutorApp. If you receive this, the email service is working correctly!";
        System.out.println("Sending test email to: " + DEFAULT_RECIPIENT);
        return sendEmail(DEFAULT_RECIPIENT, subject, testMessage);
    }

    /**
     * Send a meeting link via email
     * @param recipientEmail The recipient's email address
     * @param meetingLink The meeting link
     * @return true if the email was sent successfully, false otherwise
     */
    public static boolean sendMeetingLink(String recipientEmail, String meetingLink) {
        String subject = "Your Tutoring Session Link";

        String message = String.format(
                "Hello,\n\n" +
                        "Here is your video conference link for your upcoming tutoring session:\n\n" +
                        "%s\n\n" +
                        "Click the link above to join the meeting when it's time. No account or installation required.\n\n" +
                        "Best regards,\n" +
                        "TutorApp Team",
                meetingLink);

        return sendEmail(recipientEmail, subject, message);
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
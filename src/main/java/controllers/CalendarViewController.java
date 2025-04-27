package controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.avaibility;
import models.reservation;
import service.AvaibilityService;
import service.ReservationService;

import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

//api
import javafx.scene.input.MouseEvent;
import utils.ESpeakTTS;
import utils.SimpleMeetingLinkGenerator;
import javafx.scene.control.TextInputDialog;
import utils.GoogleMeetService;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import utils.InfobipSMSService;

public class CalendarViewController implements Initializable {

    @FXML
    private VBox calendarContainer;

    @FXML
    private Label availabilityInfoLabel;

    private avaibility currentAvailability;
    private ReservationService reservationService;
    private AvaibilityService availabilityService;

    private CalendarView calendarView;
    private Calendar reservationsCalendar;
    @FXML
    private Button confirmButton;
    @FXML
    private Button generateMeetingButton;
    private Entry<reservation> selectedEntry;

    @FXML private Button speakButton;
    @FXML private Label statusLabel;

    public void setAvailability(avaibility availability) {
        this.currentAvailability = availability;
        availabilityInfoLabel.setText("Availability: " + availability.getDate() +
                " from " + availability.getStartTime() +
                " to " + availability.getEndTime());
        loadReservations();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reservationService = new ReservationService();
        availabilityService = new AvaibilityService();

        // Set up the calendar
        setupCalendar();

        // Set up speak button
        speakButton.setOnAction(e -> {
            speakSelectedTopic();
        });
        
        // Set up generate meeting button
        if (generateMeetingButton != null) {
            generateMeetingButton.setOnAction(e -> {
                generateMeetingLink();
            });
        }
        
        // Check if TTS is available and update the status label
        boolean ttsAvailable = ESpeakTTS.isAvailable();
        statusLabel.setText("TTS: " + (ttsAvailable ? "Available ✅" : "Not Available ❌"));
        statusLabel.setStyle(ttsAvailable ? 
                            "-fx-text-fill: green; -fx-font-weight: bold;" : 
                            "-fx-text-fill: red; -fx-font-weight: bold;");
        System.out.println("TTS status in CalendarView initialization: " + ttsAvailable);
    }
    
    private void speakSelectedTopic() {
        if (selectedEntry != null) {
            reservation res = selectedEntry.getUserObject();
            ESpeakTTS.speak(res.getTopic());
        } else {
            showAlert("No Selection", "Please select a reservation first", AlertType.WARNING);
        }
    }

    private void setupCalendar() {
        calendarView = new CalendarView();
        CalendarSource source = new CalendarSource("Reservations");
        reservationsCalendar = new Calendar("Reservations");
        reservationsCalendar.setStyle(Style.STYLE1);
        source.getCalendars().add(reservationsCalendar);
        calendarView.getCalendarSources().add(source);

        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSourceTrayButton(false);
        calendarView.setShowPageToolBarControls(false);

        // Add entry factory to customize entry appearance
        // Set up entry interaction
        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry.getUserObject() instanceof reservation) {
                // Set the selectedEntry when clicked
                selectedEntry = (Entry<reservation>) entry;
                reservation res = (reservation) entry.getUserObject();
                
                // Speak the topic when clicked
                ESpeakTTS.speak(res.getTopic());
                
                // Show details
                showReservationDetails(res);
            }
            return null;
        });


        // Add context menu for toggling confirmation
        calendarView.setEntryContextMenuCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry != null && entry.getUserObject() instanceof reservation) {
                return createReservationContextMenu((Entry<reservation>) entry);
            }
            return null;
        });

        calendarContainer.getChildren().add(calendarView);
        
        // Add a notice about speech functionality
        showAlert("Speech Feature", "Click on any reservation to hear its topic spoken", AlertType.INFORMATION);
    }

    private ContextMenu createReservationContextMenu(Entry<reservation> entry) {
        ContextMenu menu = new ContextMenu();
        reservation res = entry.getUserObject();

        MenuItem toggleConfirmItem = new MenuItem();

        if ("Confirmed".equals(res.getStatus())) {
            toggleConfirmItem.setText("Unconfirm Reservation");
        } else {
            toggleConfirmItem.setText("Confirm Reservation");
        }

        toggleConfirmItem.setOnAction(e -> toggleReservationStatus(entry, res));

        menu.getItems().add(toggleConfirmItem);
        return menu;
    }

    private void customizeEntryAppearance(Entry<reservation> entry) {
        reservation res = entry.getUserObject();

        // Create title with TTS indicator
        String baseTitle = res.getTopic();
        String statusPrefix = "";
        
        // Add checkmark to the title based on status
        if ("Confirmed".equals(res.getStatus())) {
            statusPrefix = "✓ ";
            entry.getStyleClass().add("confirmed-reservation");
        } else {
            entry.getStyleClass().add("pending-reservation");
        }
        
        // Add speech icon to indicate it's speakable
        entry.setTitle(statusPrefix + baseTitle + " 🔊");
    }

    @FXML
    private void handleConfirmReservationAction() {
        if (selectedEntry == null) {
            showAlert("No Selection", "Please select a reservation first", AlertType.WARNING);
            return;
        }

        reservation res = (reservation) selectedEntry.getUserObject();

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Reservation");
        confirmation.setHeaderText("Confirm this reservation?");
        confirmation.setContentText("Are you sure you want to confirm reservation: " + res.getTopic());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Toggle status
                    String newStatus = "Confirmed".equals(res.getStatus()) ? "Not Confirmed" : "Confirmed";
                    res.setStatus(newStatus);
                    reservationService.update(res);

                    // Update entry appearance
                    updateEntryAppearance(selectedEntry, res);

                    // If the reservation is confirmed, offer to send SMS
                    if ("Confirmed".equals(newStatus) && InfobipSMSService.isAvailable()) {
                        offerSendConfirmationSMS(res);
                    }

                    showAlert("Success", "Reservation status updated to " + newStatus, AlertType.INFORMATION);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to update reservation: " + e.getMessage(), AlertType.ERROR);
                }
            }
        });
    }

    private void updateEntryAppearance(Entry<reservation> entry, reservation res) {
        // Clear existing style classes
        entry.getStyleClass().removeAll("confirmed-reservation", "pending-reservation");

        if ("Confirmed".equals(res.getStatus())) {
            entry.getStyleClass().add("confirmed-reservation");
            // Add prefix to title if not already present
            if (!entry.getTitle().startsWith("[Confirmed] ")) {
                entry.setTitle("[Confirmed] " + entry.getTitle());
            }
        } else {
            entry.getStyleClass().add("pending-reservation");
            // Remove prefix if present
            if (entry.getTitle().startsWith("[Confirmed] ")) {
                entry.setTitle(entry.getTitle().substring("[Confirmed] ".length()));
            }
        }
    }

    private void toggleReservationStatus(Entry<reservation> entry, reservation res) {
        try {
            String newStatus = "Confirmed".equals(res.getStatus()) ? "Not Confirmed" : "Confirmed";
            res.setStatus(newStatus);
            reservationService.update(res);

            // Update the entry appearance
            customizeEntryAppearance(entry);

            showAlert("Success",
                    "Reservation status changed to " + newStatus,
                    AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error",
                    "Failed to update reservation status: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void loadReservations() {
        if (currentAvailability == null) {
            return;
        }

        try {
            // Clear existing entries
            reservationsCalendar.clear();

            // Get all reservations for this availability
            List<reservation> reservationList = reservationService.getReservationsByAvaibilityId(currentAvailability.getId());

            // Add each reservation as a calendar entry
            for (reservation res : reservationList) {
                try {
                    // Create entry with reservation topic as title
                    Entry<reservation> entry = new Entry<>(res.getTopic());
                    
                    // Store the reservation object in the entry for reference
                    entry.setUserObject(res);

                    // Set time from availability and reservation
                    Date availabilityDate = new SimpleDateFormat("yyyy-MM-dd").parse(currentAvailability.getDate());
                    LocalDate localDate = convertToLocalDateTime(availabilityDate).toLocalDate();

                    // Get start time and calculate end time based on duration
                    Date startTime = res.getStart_time();
                    LocalDateTime startDateTime = convertToLocalDateTime(startTime);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(res.getDuration());

                    // Create start and end times for the entry
                    entry.setInterval(startDateTime, endDateTime);

                    // Apply custom appearance based on status
                    customizeEntryAppearance(entry);
                    
                    // Add entry to calendar
                    reservationsCalendar.addEntry(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Set calendar date to availability date
            try {
                Date availabilityDate = new SimpleDateFormat("yyyy-MM-dd").parse(currentAvailability.getDate());
                LocalDate localDate = convertToLocalDateTime(availabilityDate).toLocalDate();
                calendarView.setToday(localDate);
                calendarView.setDate(localDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load reservations: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @FXML
    private void handleMonthViewAction() {
        calendarView.showMonthPage();
    }

    @FXML
    private void handleWeekViewAction() {
        calendarView.showWeekPage();
    }

    @FXML
    private void handleDayViewAction() {
        calendarView.showDayPage();
    }

    @FXML
    private void handleBackAction() {
        // Close this view and return to previous screen
        Stage stage = (Stage) calendarContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAddReservationAction() {
        // TODO: Implementation for adding a new reservation
        // This would typically open a form to create a new reservation
    }

    @FXML
    private void handleRefreshAction() {
        loadReservations();
    }

    private void showReservationDetails(reservation res) {
        if (res == null) {
            return;
        }

        // Create and show a detail dialog
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Reservation Details");
        alert.setHeaderText("Reservation: " + res.getTopic());

        String content = "ID: " + res.getId() + "\n" +
                "Topic: " + res.getTopic() + "\n" +
                "Start Time: " + res.getStart_time() + "\n" +
                "Duration: " + res.getDuration() + " minutes\n" +
                "Status: " + res.getStatus() + "\n" +
                "Availability ID: " + res.getAvaibility_id();

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleTopicClick(MouseEvent event) {
        if (selectedEntry != null) {
            reservation res = selectedEntry.getUserObject();
            if (res != null) {
                // Speak the topic of the selected reservation
                boolean success = ESpeakTTS.speak(res.getTopic());
                if (success) {
                    showAlert("Speaking", "Speaking topic: " + res.getTopic(), AlertType.INFORMATION);
                }
            }
        } else {
            showAlert("No Selection", "Please select a reservation first", AlertType.WARNING);
        }
    }

    /**
     * Generate a meeting link for the selected reservation
     */
    private void generateMeetingLink() {
        if (selectedEntry == null) {
            showAlert("No Selection", "Please select a reservation first", AlertType.WARNING);
            return;
        }

        reservation res = selectedEntry.getUserObject();
        
        // Generate a meeting link using Jitsi (no authentication required)
        String meetingLink = SimpleMeetingLinkGenerator.generateMeetingLink(res.getTopic());
        
        // Show dialog with the link
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Meeting Link");
        alert.setHeaderText("Video Conference Link Generated");
        
        // Create content with the link
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label infoLabel = new Label("Share this link with participants:");
        Hyperlink link = new Hyperlink(meetingLink);
        link.setOnAction(e -> {
            try {
                // Open the link in the default browser
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", meetingLink});
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add a copy button
        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(meetingLink);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
            showAlert("Copied", "Link copied to clipboard", AlertType.INFORMATION);
        });
        
        // Add a button to send the link via SMS if the service is available
        if (InfobipSMSService.isAvailable()) {
            Button smsButton = new Button("Send Link via SMS");
            smsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            smsButton.setOnAction(e -> {
                sendMeetingLinkViaSMS(meetingLink);
                alert.close();
            });
            content.getChildren().addAll(infoLabel, link, copyButton, smsButton);
        } else {
            content.getChildren().addAll(infoLabel, link, copyButton);
        }
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    /**
     * Offer to send a confirmation SMS when a reservation is confirmed
     */
    private void offerSendConfirmationSMS(reservation res) {
        Alert smsAlert = new Alert(AlertType.CONFIRMATION);
        smsAlert.setTitle("Send Confirmation SMS");
        smsAlert.setHeaderText("Would you like to send a confirmation SMS?");
        smsAlert.setContentText("Send a confirmation SMS to the student for this reservation?");
        
        smsAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Ask for phone number
                TextInputDialog phoneDialog = new TextInputDialog("+33");
                phoneDialog.setTitle("Phone Number");
                phoneDialog.setHeaderText("Enter student's phone number");
                phoneDialog.setContentText("Phone number (with country code):");
                
                phoneDialog.showAndWait().ifPresent(phoneNumber -> {
                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        // Get student name
                        TextInputDialog nameDialog = new TextInputDialog("");
                        nameDialog.setTitle("Student Name");
                        nameDialog.setHeaderText("Enter student's name");
                        nameDialog.setContentText("Name:");
                        
                        nameDialog.showAndWait().ifPresent(studentName -> {
                            if (studentName != null && !studentName.trim().isEmpty()) {
                                // Format date and time
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                String date = dateFormat.format(res.getStart_time());
                                String time = timeFormat.format(res.getStart_time());
                                
                                // Get tutor name (or use default)
                                String tutorName = "your tutor";
                                try {
                                    if (currentAvailability != null) {
                                        int tutorId = currentAvailability.getTutorId();
                                        tutorName = "Tutor #" + tutorId;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                
                                // Send the confirmation SMS
                                boolean sent = InfobipSMSService.sendReservationConfirmation(
                                        phoneNumber, 
                                        studentName, 
                                        tutorName, 
                                        date, 
                                        time, 
                                        res.getTopic());
                                
                                if (sent) {
                                    showAlert("SMS Sent", "Confirmation SMS sent to " + phoneNumber, AlertType.INFORMATION);
                                    
                                    // Ask if they also want to generate a meeting link
                                    offerGenerateMeetingLink(phoneNumber);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Offer to generate a meeting link after sending a confirmation SMS
     */
    private void offerGenerateMeetingLink(String phoneNumber) {
        Alert meetingAlert = new Alert(AlertType.CONFIRMATION);
        meetingAlert.setTitle("Create Meeting Link");
        meetingAlert.setHeaderText("Would you like to create a meeting link?");
        meetingAlert.setContentText("Generate a video conference link for this session?");
        
        meetingAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Generate the meeting link
                if (selectedEntry != null) {
                    reservation res = selectedEntry.getUserObject();
                    String meetingLink = SimpleMeetingLinkGenerator.generateMeetingLink(res.getTopic());
                    
                    // Ask if they want to send the link via SMS
                    Alert smsLinkAlert = new Alert(AlertType.CONFIRMATION);
                    smsLinkAlert.setTitle("Send Meeting Link");
                    smsLinkAlert.setHeaderText("Send the meeting link via SMS?");
                    smsLinkAlert.setContentText("Send the video conference link to the student's phone?");
                    
                    smsLinkAlert.showAndWait().ifPresent(smsResponse -> {
                        if (smsResponse == ButtonType.OK) {
                            // Send the meeting link via SMS
                            boolean sent = InfobipSMSService.sendMeetingLink(phoneNumber, meetingLink);
                            if (sent) {
                                showAlert("Link Sent", "Meeting link sent via SMS", AlertType.INFORMATION);
                            }
                        }
                        
                        // Show the meeting link anyway
                        showMeetingLinkAlert(meetingLink);
                    });
                }
            }
        });
    }
    
    /**
     * Show an alert with the meeting link
     */
    private void showMeetingLinkAlert(String meetingLink) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Meeting Link");
        alert.setHeaderText("Video Conference Link Generated");
        
        // Create content with the link
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label infoLabel = new Label("Here is your meeting link:");
        Hyperlink link = new Hyperlink(meetingLink);
        link.setOnAction(e -> {
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", meetingLink});
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(meetingLink);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
            showAlert("Copied", "Link copied to clipboard", AlertType.INFORMATION);
        });
        
        content.getChildren().addAll(infoLabel, link, copyButton);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
    
    /**
     * Send a meeting link via SMS
     */
    private void sendMeetingLinkViaSMS(String meetingLink) {
        TextInputDialog phoneDialog = new TextInputDialog("+33");
        phoneDialog.setTitle("Send Meeting Link");
        phoneDialog.setHeaderText("Enter recipient's phone number");
        phoneDialog.setContentText("Phone number (with country code):");
        
        phoneDialog.showAndWait().ifPresent(phoneNumber -> {
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                boolean sent = InfobipSMSService.sendMeetingLink(phoneNumber, meetingLink);
                if (sent) {
                    showAlert("SMS Sent", "Meeting link sent to " + phoneNumber, AlertType.INFORMATION);
                }
            }
        });
    }
}
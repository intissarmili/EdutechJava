package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.reservation;
import service.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.sql.SQLException;

public class AjouterReservationController implements Initializable {

    @FXML
    private TextField topicField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Integer> hourComboBox;

    @FXML
    private ComboBox<Integer> minuteComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Spinner<Integer> durationSpinner;
    @FXML private Label topicErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label timeErrorLabel;
    @FXML private Label statusErrorLabel;
    @FXML private Label durationErrorLabel;

  /*  @FXML
    private ComboBox<Integer> availabilityComboBox;*/

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize hour combo box with values 0-23
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        hourComboBox.setItems(hours);

        // Initialize minute combo box with values 0, 15, 30, 45
        ObservableList<Integer> minutes = FXCollections.observableArrayList(0, 15, 30, 45);
        minuteComboBox.setItems(minutes);

        // Initialize status combo box
        ObservableList<String> statusOptions =
                FXCollections.observableArrayList("Confirmed", "Pending", "Cancelled");
        statusComboBox.setItems(statusOptions);

        // Initialize duration spinner (15-180 minutes, increment by 15)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 180, 30, 15);
        durationSpinner.setValueFactory(valueFactory);

        // Set default values
        datePicker.setValue(LocalDate.now());
        hourComboBox.setValue(9); // Default to 9 AM
        minuteComboBox.setValue(0); // Default to 00 minutes
        statusComboBox.setValue("Pending");

        // Load availabilities from database
   //     loadAvailabilities();
    }

  /*  private void loadAvailabilities() {
        try {
            // In a real application, you would fetch availability IDs from your database
            // For simplicity, we're just adding some example IDs
            ObservableList<Integer> availabilityIds = FXCollections.observableArrayList(1, 2, 3, 4, 5);
            availabilityComboBox.setItems(availabilityIds);

            // Set default selection
            if (!availabilityIds.isEmpty()) {
                availabilityComboBox.setValue(availabilityIds.get(0));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load availabilities: " + e.getMessage());
        }
    }*/

    @FXML
    void saveReservationAction(ActionEvent event) {
        // Clear all previous error messages first
        topicErrorLabel.setText("");
        dateErrorLabel.setText("");
        timeErrorLabel.setText("");
        statusErrorLabel.setText("");
        durationErrorLabel.setText("");

        boolean hasError = false;

        try {
            // Validate Topic
            String topic = topicField.getText().trim();
            if (topic.isEmpty()) {
                topicErrorLabel.setText("Topic cannot be empty.");
                hasError = true;
            } else if (topic.length() < 20 || topic.length() > 150) {
                topicErrorLabel.setText("Topic must be between 20 and 150 characters.");
                hasError = true;
            }

            // Validate Date
            LocalDate date = datePicker.getValue();
            LocalDate today = LocalDate.now();
            if (date == null) {
                dateErrorLabel.setText("Please select a reservation date.");
                hasError = true;
            } else if (date.isBefore(today)) {
                dateErrorLabel.setText("The date cannot be in the past.");
                hasError = true;
            } else if (date.isAfter(today.plusWeeks(2))) {
                dateErrorLabel.setText("The date must be within the next 2 weeks.");
                hasError = true;
            }

            // Validate Time
            Integer hour = hourComboBox.getValue();
            Integer minute = minuteComboBox.getValue();
            if (hour == null || minute == null) {
                timeErrorLabel.setText("Please select a valid time.");
                hasError = true;
            }

            // Validate Status
            String status = statusComboBox.getValue();
            if (status == null || status.trim().isEmpty()) {
                statusErrorLabel.setText("Please select a reservation status.");
                hasError = true;
            }

            // Validate Duration
            Integer duration = durationSpinner.getValue();
            if (duration == null) {
                durationErrorLabel.setText("Please select a valid duration.");
                hasError = true;
            }

            // If any error was found, stop here
            if (hasError) return;

            // Combine date and time
            LocalDateTime dateTime = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
            Date startTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

            // Temporary hardcoded availability_id
            int availability_id = 20;

            // Create the reservation
            reservation newReservation = new reservation(topic, startTime, status, duration, availability_id);
            ReservationService reservationService = new ReservationService();
            reservationService.add(newReservation);

            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Reservation Added", "Reservation added successfully!");


            // Optional: Navigate to detail view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/details.fxml"));
            Parent root = loader.load(); // This line actually loads the FXML
            DetailReservationController controller = loader.getController();
            controller.setReservation(newReservation); // Pass the reservation object
            topicField.getScene().setRoot(root); // Now this works, since 'root' is defined



        } catch (SQLException e) {
            e.printStackTrace();
            // You can also add a hidden label in FXML for unexpected errors if needed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    void cancelAction(ActionEvent event) {
        try {
            // Navigate back to the main view or list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListReservations.fxml"));
            Parent root = loader.load();
            topicField.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
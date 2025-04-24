package controllers.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.avaibility;
import models.reservation;
import service.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class AjouterReservationController implements Initializable {

    @FXML
    private TextField topicField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> hourComboBox;

    @FXML
    private ComboBox<String> minuteComboBox;

    @FXML
    private ComboBox<Integer> durationComboBox;

    @FXML
    private Label topicErrorLabel;
    @FXML
    private Label dateErrorLabel;
    @FXML
    private Label timeErrorLabel;
    @FXML
    private Label durationErrorLabel;

    // Fields to store the  data
    private avaibility selectedAvaibility;

    public void setAvaibilityData(avaibility av) {
        this.selectedAvaibility =av ;

        // Pre-populate form fields with  data if desired
        if (selectedAvaibility != null) {
            try {
                // Parse the date string from  and set it to the DatePicker
                // Assuming the date format is "yyyy-MM-dd" or similar
                LocalDate date = LocalDate.parse(selectedAvaibility.getDate());
                datePicker.setValue(date);

                // Set the hour and minute combo boxes based on  start time
                // Assuming the time format is "HH:mm" (24-hour format)
                String startTime = selectedAvaibility.getStartTime();
                String[] timeParts = startTime.split(":");
                if (timeParts.length >= 2) {
                    hourComboBox.setValue(timeParts[0]);
                    minuteComboBox.setValue(timeParts[1]);
                }

                // You might also want to store the tutor ID for later use
                // For example, set it as a class variable
                // this.tutorId = selectedAvaibility.getTutorId();
            } catch (Exception e) {
                System.out.println("Error parsing availability data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Heures (00-23)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourComboBox.setItems(hours);

        // Minutes (00-55, de 5 en 5)
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minutes.add(String.format("%02d", i));
        }
        minuteComboBox.setItems(minutes);

        // Durée
        durationComboBox.getItems().addAll(15, 20, 30);
        durationComboBox.setValue(15);

        // Valeurs par défaut
        datePicker.setValue(LocalDate.now());
        hourComboBox.setValue("09");
        minuteComboBox.setValue("00");

        // Cacher les erreurs au début
        topicErrorLabel.setVisible(false);
        dateErrorLabel.setVisible(false);
        timeErrorLabel.setVisible(false);
        durationErrorLabel.setVisible(false);
    }

    @FXML
    void saveReservationAction(ActionEvent event) {
        topicErrorLabel.setText("");
        dateErrorLabel.setText("");
        timeErrorLabel.setText("");
        durationErrorLabel.setText("");

        boolean hasError = false;

        try {
            // Sujet
            String topic = topicField.getText().trim();
            if (topic.isEmpty()) {
                topicErrorLabel.setText("Le sujet ne peut pas être vide.");
                topicErrorLabel.setVisible(true); // <-- ajout nécessaire

                hasError = true;
            } else if (topic.length() < 20 || topic.length() > 150) {
                topicErrorLabel.setText("Le sujet doit contenir entre 20 et 150 caractères.");
                topicErrorLabel.setVisible(true); // <-- ajout nécessaire

                hasError = true;
            } else {
                topicErrorLabel.setVisible(false); // <-- pour cacher si c'est corrigé
            }

            // Date
            LocalDate date = datePicker.getValue();
            LocalDate today = LocalDate.now();
            if (date == null) {
                dateErrorLabel.setText("Veuillez choisir une date.");
                dateErrorLabel.setVisible(true);
                hasError = true;
            } else if (date.isBefore(today)) {
                dateErrorLabel.setText("La date ne peut pas être dans le passé.");
                dateErrorLabel.setVisible(true);
                hasError = true;
            } else if (date.isAfter(today.plusWeeks(2))) {
                dateErrorLabel.setText("La date doit être dans les deux semaines à venir.");
                dateErrorLabel.setVisible(true);
                hasError = true;
            }

            // Heure
            String hourStr = hourComboBox.getValue();
            String minuteStr = minuteComboBox.getValue();
            if (hourStr == null || minuteStr == null) {
                timeErrorLabel.setText("Veuillez sélectionner une heure et des minutes.");
                timeErrorLabel.setVisible(true);
                hasError = true;
            }

            // Durée
            Integer duration = durationComboBox.getValue();
            if (duration == null) {
                durationErrorLabel.setText("Veuillez choisir une durée.");
                durationErrorLabel.setVisible(true);
                hasError = true;
            }

            if (hasError) return;

            // Conversion
            int hour = Integer.parseInt(hourStr);
            int minute = Integer.parseInt(minuteStr);

            LocalDateTime dateTime = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
            Date startTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

            // Change this in the saveReservationAction method
            int availability_id = selectedAvaibility != null ? selectedAvaibility.getId() : 0;

// Then check if it's valid before proceeding
            if (availability_id == 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune disponibilité sélectionnée.");
                return;
            }

            reservation newReservation = new reservation(topic, startTime, duration, availability_id);
            ReservationService reservationService = new ReservationService();
            reservationService.add(newReservation);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation ajoutée avec succès !");

            // Rediriger vers la vue de détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/reservation/details.fxml"));
            Parent root = loader.load();
            DetailReservationController controller = loader.getController();
            controller.setReservation(newReservation);
            topicField.getScene().setRoot(root);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/reservation/ListReservations.fxml"));
            Parent root = loader.load();
            topicField.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println("Erreur de navigation : " + e.getMessage());
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

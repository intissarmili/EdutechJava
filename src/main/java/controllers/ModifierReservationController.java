package controllers;

import models.reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ModifierReservationController {

    @FXML private TextField topicField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> hourComboBox;
    @FXML private ComboBox<Integer> minuteComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Spinner<Integer> durationSpinner;

    private reservation currentReservation;

    public void setReservation(reservation r) {
        this.currentReservation = r;

        // populate fields
        topicField.setText(r.getTopic());

        Date start = r.getStart_time();
        LocalDateTime localDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        datePicker.setValue(localDateTime.toLocalDate());
        hourComboBox.setValue(localDateTime.getHour());
        minuteComboBox.setValue(localDateTime.getMinute());

        statusComboBox.setValue(r.getStatus());
        durationSpinner.getValueFactory().setValue(r.getDuration());
    }

    @FXML
    void updateReservationAction(ActionEvent event) {
        try {
            currentReservation.setTopic(topicField.getText());
            LocalDate date = datePicker.getValue();
            int hour = hourComboBox.getValue();
            int minute = minuteComboBox.getValue();
            LocalDateTime dateTime = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
            Date startTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            currentReservation.setStart_time(startTime);

            currentReservation.setStatus(statusComboBox.getValue());
            currentReservation.setDuration(durationSpinner.getValue());

            ReservationService service = new ReservationService();
            service.update(currentReservation);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailReservation.fxml"));
            Parent root = loader.load();
            DetailReservationController controller = loader.getController();
            controller.setReservation(currentReservation);
            topicField.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        // Go back to details
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailReservation.fxml"));
            Parent root = loader.load();
            DetailReservationController controller = loader.getController();
            controller.setReservation(currentReservation);
            topicField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

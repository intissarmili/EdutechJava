package controllers;

import models.reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.IntStream;

public class ModifierReservationController {

    @FXML private TextField topicField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> hourComboBox;
    @FXML private ComboBox<Integer> minuteComboBox;
    @FXML private ComboBox<Integer> durationComboBox;

    private reservation currentReservation;

    @FXML
    public void initialize() {
        // Heures : 0 à 23
        hourComboBox.getItems().addAll(IntStream.range(0, 24).boxed().toList());
        // Minutes : 0 à 59 (de 5 en 5)
        minuteComboBox.getItems().addAll(IntStream.range(0, 60).filter(i -> i % 5 == 0).boxed().toList());

        // Durée
        durationComboBox.getItems().addAll(15, 20, 30);
        durationComboBox.setValue(15);
    }

    public void setReservation(reservation r) {
        this.currentReservation = r;

        topicField.setText(r.getTopic());

        Date start = r.getStart_time();
        LocalDateTime localDateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        datePicker.setValue(localDateTime.toLocalDate());
        hourComboBox.setValue(localDateTime.getHour());
        minuteComboBox.setValue(localDateTime.getMinute());

        durationComboBox.setValue(r.getDuration());
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

            currentReservation.setDuration(durationComboBox.getValue());

            ReservationService service = new ReservationService();
            service.update(currentReservation);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/details.fxml"));
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/details.fxml"));
            Parent root = loader.load();
            DetailReservationController controller = loader.getController();
            controller.setReservation(currentReservation);
            topicField.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

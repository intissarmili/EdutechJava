package controllers.back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import models.reservation;
import service.ReservationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


public class DetailReservationController {

    @FXML
    private Label topicLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Label statusLabel;

    private reservation currentReservation;


    // Method to receive the reservation and update the labels
    public void setReservation(reservation res) {
        this.currentReservation =res;
        topicLabel.setText(res.getTopic());

        // Convert java.util.Date to LocalDateTime
        Date startTime = res.getStart_time();
        LocalDateTime dateTime = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        dateLabel.setText(dateTime.toLocalDate().toString());
        timeLabel.setText(dateTime.toLocalTime().toString());

        durationLabel.setText(res.getDuration() + " minutes");
        statusLabel.setText(res.getStatus());
    }

    @FXML
    void backAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/listCards.fxml"));
            Parent root = loader.load();
            topicLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/reservation/update.fxml"));
            Parent root = loader.load();

            ModifierReservationController controller = loader.getController();
            controller.setReservation(currentReservation); // pass the reservation to update

            topicLabel.getScene().setRoot(root); // replace with any fx:id of a node in the scene
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            // Confirm deletion (optional but nice UX)
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Reservation");
            alert.setContentText("Are you sure you want to delete this reservation?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Delete from DB
                ReservationService service = new ReservationService();
                service.delete(currentReservation.getId());

                // Redirect to home or list view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/reservation/ListReservations.fxml")); // adjust if needed
                Parent root = loader.load();
                topicLabel.getScene().setRoot(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

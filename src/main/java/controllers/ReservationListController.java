package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.reservation;
import service.ReservationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ReservationListController {

    @FXML private TableView<reservation> reservationTable;
    @FXML private TableColumn<reservation, Integer> idColumn;
    @FXML private TableColumn<reservation, String> topicColumn;
    @FXML private TableColumn<reservation, java.util.Date> startTimeColumn;
    @FXML private TableColumn<reservation, String> statusColumn;
    @FXML private TableColumn<reservation, Integer> durationColumn;

    private final ReservationService reservationService = new ReservationService();

    private int avaibilityId; // Setter à appeler depuis l'autre fenêtre

    public void setAvaibilityId(int avaibilityId) {
        this.avaibilityId = avaibilityId;
        loadReservations();
    }

    private void loadReservations() {
        try {
            List<reservation> reservations = reservationService.getReservationsByAvaibilityId(avaibilityId);

            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            topicColumn.setCellValueFactory(new PropertyValueFactory<>("topic"));
            startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("start_time"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

            reservationTable.getItems().setAll(reservations);
        } catch (SQLException e) {
            e.printStackTrace(); // À remplacer par un message d'erreur dans la GUI si besoin
        }
    }
    @FXML
    private void handleBack() {
        try {
            // Load the availability list FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/avaibility/listCards.fxml"));
            Parent root = loader.load();

            // Get current window and cast to Stage
            Window window = reservationTable.getScene().getWindow();
            if (window instanceof Stage) {
                Stage currentStage = (Stage) window;

                // Create new scene with same dimensions
                Scene scene = new Scene(root);

                // Set the scene and show
                currentStage.setScene(scene);
                currentStage.show();
            } else {
                // If for some reason we don't have a Stage, create a new one
                Stage newStage = new Stage();
                newStage.setTitle("Tutor Availability");
                newStage.setScene(new Scene(root));
                newStage.show();

                // Close the current window if possible
                if (window != null) {
                    window.hide();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
           // showAlert("Error", "Failed to return to availability list: " + e.getMessage());
        }
    }

}

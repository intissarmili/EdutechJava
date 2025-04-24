package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.reservation;
import service.ReservationService;

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
}

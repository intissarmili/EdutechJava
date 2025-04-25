package controllers.back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.reservation;
import service.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationListController implements Initializable {

    @FXML private TableView<reservation> reservationTable;
    @FXML private TableColumn<reservation, Integer> idColumn;
    @FXML private TableColumn<reservation, String> topicColumn;
    @FXML private TableColumn<reservation, String> dateColumn;
    @FXML private TableColumn<reservation, String> startTimeColumn;
    @FXML private TableColumn<reservation, String> statusColumn;
    @FXML private TableColumn<reservation, Integer> durationColumn; // Added missing column


    @FXML
    private TextField searchField;

    private List<reservation> allReservations; // Stocke toutes les réservations

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        try {
            allReservations = reservationService.getAll(); // Utilisation de l'instance
            reservationTable.getItems().setAll(allReservations);
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: Afficher un message d'erreur dans l'interface utilisateur
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            reservationTable.getItems().setAll(allReservations); // Réinitialise la liste
        } else {
            List<reservation> filteredReservations = allReservations.stream()
                .filter(res -> res.getTopic().toLowerCase().contains(searchText))
                .toList();
            reservationTable.getItems().setAll(filteredReservations);
        }
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
        reservationTable.getItems().setAll(allReservations); // Réinitialise la liste
    }
    private final ReservationService reservationService = new ReservationService();
    private int availabilityId;



    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        topicColumn.setCellValueFactory(new PropertyValueFactory<>("topic"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime")); // Fixed property name
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
    }

    public void setAvailabilityId(int availabilityId) {
        this.availabilityId = availabilityId;
        loadReservations();
    }

    public void loadReservations() {
        try {
            List<reservation> reservations;

            if (availabilityId > 0) {
                reservations = reservationService.getReservationsByAvaibilityId(availabilityId);
            } else {
                reservations = reservationService.getAll();
            }

            reservationTable.getItems().setAll(reservations);
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: Show error alert
        }
    }
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/back/avaibility/listCards.fxml"));
            Stage stage = (Stage) reservationTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
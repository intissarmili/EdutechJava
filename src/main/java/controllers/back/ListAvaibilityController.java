package controllers.back;

import controllers.back.AjouterReservationController;
import controllers.back.ReservationListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.avaibility;
import service.AvaibilityService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ListAvaibilityController {

    @FXML private TableView<avaibility> availabilityTable;
    @FXML private TableColumn<avaibility, String> dateColumn;
    @FXML private TableColumn<avaibility, String> timeColumn;
    @FXML private TableColumn<avaibility, Integer> tutorColumn;
    @FXML private StackPane contentPane;
    @FXML private AnchorPane detailsPane;

    // Details view fields
    @FXML private Label detailsDate;
    @FXML private Label detailsTime;
    @FXML private Label detailsTutor;

    private final AvaibilityService avaibilityService = new AvaibilityService();
    private final ObservableList<avaibility> availabilityData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAvailabilityData();
        showOnly(availabilityTable);
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(cellData -> {
            avaibility a = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    a.getStartTime() + " - " + a.getEndTime()
            );
        });
        tutorColumn.setCellValueFactory(new PropertyValueFactory<>("tutorId"));

        // Action buttons column
        TableColumn<avaibility, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Details");
            private final Button reserveButton = new Button("Reserve");

            {
                // Style buttons to match FXML
                detailsButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 5 10;");
                reserveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");

                // Set button actions
                detailsButton.setOnAction(event -> showDetails(getCurrentItem()));
                reserveButton.setOnAction(event -> handleReservation(getCurrentItem()));
            }

            private avaibility getCurrentItem() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10, detailsButton, reserveButton);
                    buttons.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(buttons);
                }
            }
        });

        availabilityTable.getColumns().add(actionsColumn);
    }

    private void loadAvailabilityData() {
        try {
            List<avaibility> availabilities = avaibilityService.getAll();
            availabilityData.clear();

            LocalDate today = LocalDate.now();
            for (avaibility avaibility : availabilities) {
                try {
                    LocalDate avaibilityDate = LocalDate.parse(avaibility.getDate());
                    if (!avaibilityDate.isBefore(today)) {
                        availabilityData.add(avaibility);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            availabilityTable.setItems(availabilityData);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load availabilities: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAvaibility() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/create.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load availability form: " + e.getMessage());
        }
    }

    private void handleReservation(avaibility avaibility) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/reservation/create.fxml"));
            Parent root = loader.load();

            AjouterReservationController controller = loader.getController();
            controller.setAvaibilityData(avaibility);

            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load reservation form: " + e.getMessage());
        }
    }

    private void showDetails(avaibility avaibility) {
        detailsDate.setText("Date: " + avaibility.getDate());
        detailsTime.setText("Time: " + avaibility.getStartTime() + " - " + avaibility.getEndTime());
        detailsTutor.setText("Tutor ID: " + avaibility.getTutorId());
        showOnly(detailsPane);
    }

    @FXML
    private void handleBackToList() {
        showOnly(availabilityTable);
    }

    private void showOnly(Node nodeToShow) {
        for (Node child : contentPane.getChildren()) {
            child.setVisible(child == nodeToShow);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
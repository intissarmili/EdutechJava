package controllers.back;

import controllers.AjouterReservationController;
import controllers.ReservationListController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.avaibility;
import service.AvaibilityService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ListAvaibilityController {

    @FXML private VBox cardsContainer;
    @FXML private StackPane contentPane;
    @FXML private AnchorPane addFormPane;
    @FXML private AnchorPane detailsPane;

    // Add form fields
    @FXML private TextField dateField;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField tutorIdField;

    // Details view fields
    @FXML private Label detailsDate;
    @FXML private Label detailsTime;
    @FXML private Label detailsTutor;



    private Parent addFormNode;

    private final AvaibilityService avaibilityService = new AvaibilityService();

    @FXML
    public void initialize() {
        loadAvaibilityCards();
        showOnly(getCardsScrollPane()); // Show only cards list at startup
    }

    // Update the loadAvaibilityCards method
    private void loadAvaibilityCards() {
        try {
            List<avaibility> availabilities = avaibilityService.getAll();
            cardsContainer.getChildren().clear();

            // Get current date (without time)
            LocalDate today = LocalDate.now();

            for (avaibility avaibility : availabilities) {
                // Parse the avaibility date
                LocalDate avaibilityDate;
                try {
                    avaibilityDate = LocalDate.parse(avaibility.getDate());
                } catch (Exception e) {
                    // Skip if date format is invalid
                    continue;
                }

                // Only show if avaibility date is today or in the future
                if (!avaibilityDate.isBefore(today)) {
                    AnchorPane card = createAvaibilityCard(avaibility);
                    cardsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load availabilities: " + e.getMessage());
        }
    }
    private AnchorPane createAvaibilityCard(avaibility avaibility) {
        AnchorPane card = new AnchorPane();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(400);

        Label dateLabel = new Label("Date: " + avaibility.getDate());
        dateLabel.setLayoutX(15);
        dateLabel.setLayoutY(15);

        Label timeLabel = new Label("Time: " + avaibility.getStartTime() + " - " + avaibility.getEndTime());
        timeLabel.setLayoutX(15);
        timeLabel.setLayoutY(40);

        Label tutorLabel = new Label("Tutor ID: " + avaibility.getTutorId());
        tutorLabel.setLayoutX(15);
        tutorLabel.setLayoutY(65);

        Button detailsButton = new Button("Details");
        detailsButton.setLayoutX(300);
        detailsButton.setLayoutY(15);
        detailsButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        detailsButton.setOnAction(event -> showDetails(avaibility));

        Button reserveButton = new Button("Reserve");
        reserveButton.setLayoutX(300);
        reserveButton.setLayoutY(50);
        reserveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        reserveButton.setOnAction(event -> handleReservation(avaibility));

        // Add View Reservations button
        Button viewReservationsButton = new Button("View Reservations");
        viewReservationsButton.setLayoutX(300);
        viewReservationsButton.setLayoutY(85);
        viewReservationsButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        viewReservationsButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/list.fxml"));
                Parent root = loader.load();

                // Passe l'avaibilityId au contrôleur
                ReservationListController controller = loader.getController();
                controller.setAvaibilityId(avaibility.getId());

                Stage stage = new Stage();
                stage.setTitle("Reservations for Avaibility");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        // Add Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setLayoutX(300);
        deleteButton.setLayoutY(120);
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> handleDeleteAvaibility(avaibility));

        card.getChildren().addAll(dateLabel, timeLabel, tutorLabel, detailsButton, reserveButton, viewReservationsButton, deleteButton);
        return card;
    }
    private void handleDeleteAvaibility(avaibility avaibility) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Availability");
        confirmAlert.setContentText("Are you sure you want to delete this availability?\nAll related reservations will be marked as canceled.");

        // Show the confirmation dialog and wait for user response
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Call service to delete availability with cascade
                    avaibilityService.deleteCascade(avaibility.getId());
                    showAlert("Success", "Availability deleted successfully");
                    loadAvaibilityCards(); // Refresh the list
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete availability: " + e.getMessage());
                }
            }
        });
    }

   /* private void showReservationsForAvaibility(avaibility avaibility) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/listReservations.fxml"));
            Parent root = loader.load();

            ListReservationController controller = loader.getController();
            controller.setAvaibilityId(avaibility.getId());

            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Scene scene = new Scene(root, width, height);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load reservations view: " + e.getMessage());
        }
    }*/
    @FXML
    private void handleShowReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/list.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            Scene scene = new Scene(root, width, height);

            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la liste des réservations : " + e.getMessage());
        }
    }


    // Add this method to handle redirection to reservation creation
    private void handleReservation(avaibility avaibility) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservation/create.fxml"));
            Parent root = loader.load();

            // Optional: Pass avaibility data to the reservation controller
            AjouterReservationController controller = loader.getController();
            controller.setAvaibilityData(avaibility); // You'll need to create this method in ReservationCreateController

            // Get current scene dimensions
            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            // Create new scene with same dimensions
            Scene scene = new Scene(root, width, height);

            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load reservation form: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAvaibility() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/avaibility/create.fxml"));
            Parent root = loader.load();

            // Get current scene dimensions
            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            // Create new scene with the same dimensions
            Scene scene = new Scene(root, width, height);

            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleSaveAvaibility() {
        try
        {
            avaibility newAvaibility = new avaibility(
                    0,
                    dateField.getText(),
                    startTimeField.getText(),
                    endTimeField.getText(),
                    Integer.parseInt(tutorIdField.getText())
            );

            avaibilityService.add(newAvaibility);
            showAlert("Success", "Avaibility added successfully");

            backToListView();
            loadAvaibilityCards();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid Tutor ID");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add avaibility: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelAdd() {
        backToListView();
    }

    private void showDetails(avaibility avaibility) {
        detailsDate.setText("Date: " + avaibility.getDate());
        detailsTime.setText("Time: " + avaibility.getStartTime() + " - " + avaibility.getEndTime());
        detailsTutor.setText("Tutor ID: " + avaibility.getTutorId());

        showOnly(detailsPane);
    }

    @FXML
    private void handleBackToList() {
        backToListView();
    }

    private void backToListView() {
        showOnly(getCardsScrollPane());
    }

    // Utility: Show only the selected node in StackPane
    private void showOnly(Node nodeToShow) {
        for (Node child : contentPane.getChildren()) {
            child.setVisible(child == nodeToShow);
        }
    }

    // Helper to find the ScrollPane holding the cards
    private ScrollPane getCardsScrollPane() {
        for (Node child : contentPane.getChildren()) {
            if (child instanceof ScrollPane && ((ScrollPane) child).getContent() == cardsContainer) {
                return (ScrollPane) child;
            }
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

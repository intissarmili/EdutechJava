package controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import models.avaibility;
import service.AvaibilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
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
    // Add filter controls

    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;
    // Filter control
    @FXML private DatePicker dateFilter;


    private Parent addFormNode;

    private final AvaibilityService avaibilityService = new AvaibilityService();

    @FXML
    public void initialize() {
        setupDateFilter();
        loadAvaibilityCards();
        showOnly(getCardsScrollPane());
    }

    // Update setupDateFilters method:
// Update setupDateFilters method:
    private void setupDateFilter() {  // Renamed from setupDateFilters
        // Set default date format
        dateFilter.setConverter(new LocalDateStringConverter());

        // Set initial date (today)
        dateFilter.setValue(LocalDate.now());

        // Handle filter button click
        filterButton.setOnAction(event -> loadAvaibilityCards());

        // Handle clear filter button
        clearFilterButton.setOnAction(event -> {
            dateFilter.setValue(null);
            loadAvaibilityCards();
        });
    }    // Custom StringConverter for DatePicker
    // Custom StringConverter for DatePicker
    private static class LocalDateStringConverter extends StringConverter<LocalDate> {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public String toString(LocalDate date) {
            if (date != null) {
                return dateFormatter.format(date);
            } else {
                return "";
            }
        }

        @Override
        public LocalDate fromString(String string) {
            if (string != null && !string.isEmpty()) {
                return LocalDate.parse(string, dateFormatter);
            } else {
                return null;
            }
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
        viewReservationsButton.setStyle("-fx-background-color: #999893; -fx-text-fill: white;");
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

        // Add Calendar Button with icon
        Button calendarButton = new Button();
        calendarButton.setLayoutX(240);  // Position to the left of other buttons
        calendarButton.setLayoutY(85);   // Same height as "View Reservations" button
        calendarButton.setLayoutY(85);   // Same height as "View Reservations" button
        calendarButton.setPrefSize(40, 25);  // Small square button for icon


        calendarButton.setText("📅");


        calendarButton.setTooltip(new Tooltip("View Calendar"));
        calendarButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        calendarButton.setOnAction(event -> openCalendarView(avaibility));

        // Add Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setLayoutX(300);
        deleteButton.setLayoutY(120);
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteButton.setOnAction(event -> handleDeleteAvaibility(avaibility));

        card.getChildren().addAll(dateLabel, timeLabel, tutorLabel, detailsButton, reserveButton,
                viewReservationsButton, calendarButton, deleteButton);
        return card;
    }

    private void loadAvaibilityCards() {
        try {
            List<avaibility> availabilities = avaibilityService.getAll();
            cardsContainer.getChildren().clear();

            LocalDate selectedDate = dateFilter.getValue();
            LocalDate today = LocalDate.now();

            for (avaibility avaibility : availabilities) {
                try {
                    LocalDate avaibilityDate = LocalDate.parse(avaibility.getDate());

                    // Apply filters
                    boolean shouldShow = true;

                    // Filter by selected date if one is selected
                    if (selectedDate != null && !avaibilityDate.isEqual(selectedDate)) {
                        shouldShow = false;
                    }

                    // Always filter out past dates
                    if (avaibilityDate.isBefore(today)) {
                        shouldShow = false;
                    }

                    if (shouldShow) {
                        AnchorPane card = createAvaibilityCard(avaibility);
                        cardsContainer.getChildren().add(card);
                    }
                } catch (Exception e) {
                    // Skip if date format is invalid
                    continue;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load availabilities: " + e.getMessage());
        }
    }
    private void openCalendarView(avaibility availability) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/avaibility/CalendarView.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the availability
            CalendarViewController controller = loader.getController();
            controller.setAvailability(availability);

            // Show the view in a new window
            Stage stage = new Stage();
            stage.setTitle("Calendar View - " + availability.getDate());
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(contentPane.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open calendar view: " + e.getMessage());
        }
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
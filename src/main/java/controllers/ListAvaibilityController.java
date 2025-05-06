package controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
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

    @FXML private FlowPane cardsContainer;
    @FXML private StackPane contentPane;
    @FXML private AnchorPane detailsPane;
    @FXML private Button addButton;

    // Details view fields
    @FXML private Label detailsDate;
    @FXML private Label detailsTime;
    @FXML private Label detailsTutor;
    
    // Filter controls
    @FXML private DatePicker dateFilter;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;

    private final AvaibilityService avaibilityService = new AvaibilityService();

    @FXML
    public void initialize() {
        setupDateFilter();
        loadAvaibilityCards();
        showOnly(getCardsScrollPane());
        
        // Style the Add Availability button - simplified
        addButton.setStyle("-fx-background-color: #1CACD2; -fx-text-fill: white; -fx-background-radius: 5px;");
    }

    private void setupDateFilter() {
        // Style the date picker - simplified
        dateFilter.setStyle("-fx-background-color: white; -fx-background-radius: 4px; -fx-border-color: #e0e0e0; -fx-border-radius: 4px;");
        dateFilter.setPromptText("Sélectionner une date");
        
        // Set date converter
        dateFilter.setConverter(new LocalDateStringConverter());
        
        // Style the Apply Filter button - simplified
        filterButton.setStyle("-fx-background-color: #1CACD2; -fx-text-fill: white; -fx-background-radius: 4px;");
        
        // Style the Show All button - simplified
        clearFilterButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #555; -fx-background-radius: 4px; -fx-border-color: #e0e0e0; -fx-border-radius: 4px; -fx-border-width: 1px;");
        
        // Set initial date (today)
        dateFilter.setValue(LocalDate.now());
        
        // Handle filter button click
        filterButton.setOnAction(event -> loadAvaibilityCards());
        
        // Handle clear filter button
        clearFilterButton.setOnAction(event -> {
            dateFilter.setValue(null);
            loadAvaibilityCards();
        });
    }

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
        // Create card with gradient background from CSS styles
        AnchorPane card = new AnchorPane();
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setPrefHeight(180);  // Shorter height to match screenshot

        // Create content container using VBox
        VBox contentBox = new VBox(10);
        contentBox.getStyleClass().add("card-content");
        contentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        contentBox.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
        AnchorPane.setTopAnchor(contentBox, 0.0);
        AnchorPane.setLeftAnchor(contentBox, 0.0);
        AnchorPane.setRightAnchor(contentBox, 0.0);
        AnchorPane.setBottomAnchor(contentBox, 0.0);
        
        // Create date header with larger font
        Label dateLabel = new Label("Date: " + avaibility.getDate());
        dateLabel.getStyleClass().add("card-title");
        dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Add time info
        Label timeLabel = new Label("Horaire: " + avaibility.getStartTime() + " - " + avaibility.getEndTime());
        timeLabel.getStyleClass().add("card-para");
        
        // Add tutor info
        Label tutorLabel = new Label("Tuteur ID: " + avaibility.getTutorId());
        tutorLabel.getStyleClass().add("card-para");
        
        // Add spacer to push buttons to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create button container - just 2 buttons like in screenshot
        HBox buttonBox = new HBox(20); // Wider spacing
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Details button with eye icon (icon only)
        Button detailsButton = new Button("👁");
        detailsButton.setTooltip(new Tooltip("Voir les détails"));
        detailsButton.getStyleClass().addAll("card-button", "icon-only-button");
        detailsButton.setStyle("-fx-background-color: #1CACD2; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
        detailsButton.setOnAction(event -> showDetails(avaibility));
        
        // Reserve button with reserve icon (icon only)
        Button reserveButton = new Button("✅");
        reserveButton.setTooltip(new Tooltip("Réserver"));
        reserveButton.getStyleClass().addAll("card-button", "icon-only-button");
        reserveButton.setStyle("-fx-background-color: #1CACD2; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
        reserveButton.setOnAction(event -> handleReservation(avaibility));
        
        // Calendar button with calendar icon (icon only)
        Button calendarButton = new Button("📅");
        calendarButton.setTooltip(new Tooltip("Voir le calendrier"));
        calendarButton.getStyleClass().addAll("card-button", "icon-only-button");
        calendarButton.setStyle("-fx-background-color: #1CACD2; -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
        calendarButton.setOnAction(event -> openCalendarView(avaibility));
        
        // Add buttons to button container
        buttonBox.getChildren().addAll(detailsButton, reserveButton, calendarButton);
        
        // Add simple X button for delete at top right (white without background)
        Button deleteButton = new Button("X");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        deleteButton.setTooltip(new Tooltip("Supprimer"));
        deleteButton.setOnAction(event -> handleDeleteAvaibility(avaibility));
        
        // Position delete button at top-right corner
        AnchorPane.setTopAnchor(deleteButton, 5.0);
        AnchorPane.setRightAnchor(deleteButton, 5.0);
        
        // Add all elements to content box
        contentBox.getChildren().addAll(dateLabel, timeLabel, tutorLabel, spacer, buttonBox);
        
        // Add content box and delete button to card
        card.getChildren().addAll(contentBox, deleteButton);
        
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
            showAlert("Erreur", "Échec du chargement des disponibilités: " + e.getMessage());
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
            stage.setTitle("Vue Calendrier - " + availability.getDate());
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(contentPane.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'ouverture de la vue calendrier: " + e.getMessage());
        }
    }

    private void handleDeleteAvaibility(avaibility avaibility) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText("Supprimer la disponibilité");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette disponibilité?\nToutes les réservations associées seront marquées comme annulées.");

        // Show the confirmation dialog and wait for user response
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Call service to delete availability with cascade
                    avaibilityService.deleteCascade(avaibility.getId());
                    showAlert("Succès", "Disponibilité supprimée avec succès");
                    loadAvaibilityCards(); // Refresh the list
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Échec de la suppression de la disponibilité: " + e.getMessage());
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
            showAlert("Erreur", "Impossible de charger le formulaire de réservation: " + e.getMessage());
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
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void showDetails(avaibility avaibility) {
        detailsDate.setText("Date: " + avaibility.getDate());
        detailsTime.setText("Horaire: " + avaibility.getStartTime() + " - " + avaibility.getEndTime());
        detailsTutor.setText("Tuteur ID: " + avaibility.getTutorId());

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
            if (child instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) child;
                if (scrollPane.getContent() instanceof FlowPane) {
                    return scrollPane;
                }
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
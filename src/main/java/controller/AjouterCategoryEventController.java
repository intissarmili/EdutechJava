package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.CategoryEvent;
import service.CategoryEventService;

public class AjouterCategoryEventController {

    @FXML
    private TextField tfLocation;

    @FXML
    private TextField tfType;

    @FXML
    private TextField tfDuration;

    @FXML
    private Button addButton;

    @FXML
    private Label locationErrorLabel;

    @FXML
    private Label typeErrorLabel;

    @FXML
    private Label durationErrorLabel;

    private final CategoryEventService service = new CategoryEventService();

    @FXML
    public void initialize() {
        setupValidators();
        addButton.setDisable(true);
    }

    private void setupValidators() {
        // Validation pour le champ Location (minimum 6 caractères)
        tfLocation.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.length() >= 6;
            validateField(tfLocation, locationErrorLabel, isValid);
            validateAllFields();
        });

        // Validation pour le champ Type (minimum 6 caractères)
        tfType.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.length() >= 6;
            validateField(tfType, typeErrorLabel, isValid);
            validateAllFields();
        });

        // Validation pour le champ Duration (minimum 60:00)
        tfDuration.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = isValidDuration(newValue);
            validateField(tfDuration, durationErrorLabel, isValid);
            validateAllFields();
        });
    }

    private boolean isValidDuration(String duration) {
        if (duration == null || !duration.matches("\\d{2}:\\d{2}")) {
            return false;
        }

        try {
            String[] parts = duration.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int totalMinutes = hours * 60 + minutes;
            return totalMinutes >= 60;
        } catch (Exception e) {
            return false;
        }
    }

    private void validateField(TextField field, Label errorLabel, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 1.5px; -fx-border-radius: 5;");
            errorLabel.setVisible(false);
        } else {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 1.5px; -fx-border-radius: 5;");
            errorLabel.setVisible(true);
        }
    }

    private void validateAllFields() {
        boolean locationValid = tfLocation.getText().length() >= 6;
        boolean typeValid = tfType.getText().length() >= 6;
        boolean durationValid = isValidDuration(tfDuration.getText());

        addButton.setDisable(!(locationValid && typeValid && durationValid));

        // Style du bouton selon l'état
        if (addButton.isDisabled()) {
            addButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        } else {
            addButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        }
    }

    @FXML
    private void handleAdd() {
        try {
            if (tfLocation.getText().length() < 6) {
                showAlert("Erreur", "Le lieu doit contenir au moins 6 caractères.", Alert.AlertType.ERROR);
                return;
            }

            if (tfType.getText().length() < 6) {
                showAlert("Erreur", "Le type doit contenir au moins 6 caractères.", Alert.AlertType.ERROR);
                return;
            }

            if (!isValidDuration(tfDuration.getText())) {
                showAlert("Erreur", "La durée doit être au format HH:MM et d'au moins 60 minutes.", Alert.AlertType.ERROR);
                return;
            }

            String location = tfLocation.getText();
            String type = tfType.getText();
            String duration = tfDuration.getText();

            CategoryEvent category = new CategoryEvent(location, type, duration);
            service.add(category);

            showAlert("Succès", "Catégorie d'événement ajoutée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception ex) {
            showAlert("Erreur", "Veuillez vérifier les données saisies.\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) tfLocation.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Style de l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("myDialog");

        alert.showAndWait();
    }
}
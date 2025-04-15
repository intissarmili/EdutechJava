package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private final CategoryEventService service = new CategoryEventService();

    @FXML
    public void initialize() {
        setupValidators();
        addButton.setDisable(true);
    }

    private void setupValidators() {
        // Validation pour le champ Location (minimum 6 caractères)
        tfLocation.textProperty().addListener((observable, oldValue, newValue) -> {
            validateField(tfLocation, newValue.length() >= 6);
            validateAllFields();
        });

        // Validation pour le champ Type (minimum 6 caractères)
        tfType.textProperty().addListener((observable, oldValue, newValue) -> {
            validateField(tfType, newValue.length() >= 6);
            validateAllFields();
        });

        // Validation pour le champ Duration (minimum 60:00)
        tfDuration.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = isValidDuration(newValue);
            validateField(tfDuration, isValid);
            validateAllFields();
        });
    }

    private boolean isValidDuration(String duration) {
        // Vérifie si la durée est au format HH:MM et est d'au moins "60:00"
        if (duration == null || !duration.matches("\\d{2}:\\d{2}")) {
            return false;
        }

        try {
            String[] parts = duration.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            // Convertir en minutes totales pour comparer
            int totalMinutes = hours * 60 + minutes;
            return totalMinutes >= 60; // Au moins 60 minutes (1 heure)
        } catch (Exception e) {
            return false;
        }
    }

    private void validateField(TextField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } else {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    private void validateAllFields() {
        boolean locationValid = tfLocation.getText().length() >= 6;
        boolean typeValid = tfType.getText().length() >= 6;
        boolean durationValid = isValidDuration(tfDuration.getText());

        // Activer/désactiver le bouton en fonction de la validité de tous les champs
        addButton.setDisable(!(locationValid && typeValid && durationValid));
    }

    @FXML
    private void handleAdd() {
        try {
            // Validation finale avant ajout
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
        alert.showAndWait();
    }
}
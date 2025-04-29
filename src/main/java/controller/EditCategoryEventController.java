package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.CategoryEvent;
import service.CategoryEventService;

public class EditCategoryEventController {

    @FXML private TextField tfLocation;
    @FXML private TextField tfType;
    @FXML private TextField tfDuration;
    @FXML private Button updateButton;
    @FXML private Label locationErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label durationErrorLabel;

    private final CategoryEventService service = new CategoryEventService();
    private CategoryEvent categoryToEdit;

    public void initData(CategoryEvent category) {
        this.categoryToEdit = category;
        tfLocation.setText(category.getLocation());
        tfType.setText(category.getType());
        tfDuration.setText(category.getDuration());

        // Force la validation initiale
        Platform.runLater(this::validateAllFields);
    }

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur EditCategoryEvent");
        setupValidators();
    }

    private void setupValidators() {
        // Validation pour le lieu
        tfLocation.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && newVal.length() >= 6;
            locationErrorLabel.setVisible(!isValid);
            validateAllFields();
        });

        // Validation pour le type
        tfType.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && newVal.length() >= 6;
            typeErrorLabel.setVisible(!isValid);
            validateAllFields();
        });

        // Validation pour la durée
        tfDuration.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = isValidDuration(newVal);
            durationErrorLabel.setVisible(!isValid);
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
            return (hours * 60 + minutes) >= 60;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateAllFields() {
        boolean locationValid = tfLocation.getText() != null && tfLocation.getText().length() >= 6;
        boolean typeValid = tfType.getText() != null && tfType.getText().length() >= 6;
        boolean durationValid = isValidDuration(tfDuration.getText());

        boolean allValid = locationValid && typeValid && durationValid;
        updateButton.setDisable(!allValid);

        System.out.println("Validation - Lieu: " + locationValid +
                ", Type: " + typeValid +
                ", Durée: " + durationValid +
                ", Bouton activé: " + allValid);

        return allValid;
    }

    @FXML
    private void handleUpdate() {
        System.out.println("Bouton Modifier cliqué");

        if (!validateAllFields()) {
            showAlert("Erreur", "Veuillez corriger les erreurs avant de soumettre", Alert.AlertType.ERROR);
            return;
        }

        try {
            System.out.println("Tentative de mise à jour...");
            categoryToEdit.setLocation(tfLocation.getText());
            categoryToEdit.setType(tfType.getText());
            categoryToEdit.setDuration(tfDuration.getText());

            service.update(categoryToEdit);
            showAlert("Succès", "Catégorie mise à jour avec succès", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception ex) {
            System.out.println("Erreur lors de la mise à jour: " + ex.getMessage());
            ex.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour: " + ex.getMessage(), Alert.AlertType.ERROR);
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
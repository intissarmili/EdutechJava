package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Certification;
import services.CertificationService;

import java.sql.SQLException;

public class addCertificationController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField prixField;
    @FXML private TextField prixPieceField;
    @FXML private TextField imgField;
    @FXML private Button cancelBtn;
    @FXML private Button addBtn;

    private CertificationService certificationService;

    @FXML
    public void initialize() {
        certificationService = new CertificationService();
    }

    @FXML
    private void handleAddCertification() {
        if (!validateInputs()) {
            return;
        }

        try {
            Certification certification = new Certification(
                    0, // ID sera généré automatiquement
                    nomField.getText(),
                    descriptionField.getText(),
                    Double.parseDouble(prixField.getText()),
                    imgField.getText(),
                    Integer.parseInt(prixPieceField.getText())
            );

            certificationService.create(certification);
            showAlert("Succès", "Ajout réussi", "La certification a été ajoutée avec succès.", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout", e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format invalide", "Veuillez entrer des valeurs numériques valides pour le prix et le prix pièce.", Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Validation du nom
        if (nomField.getText().isEmpty() || nomField.getText().length() < 5) {
            errors.append("- Le nom doit contenir au moins 5 caractères\n");
        }

        // Validation de la description
        if (descriptionField.getText().isEmpty() || descriptionField.getText().length() < 5) {
            errors.append("- La description doit contenir au moins 5 caractères\n");
        }

        // Validation du prix
        try {
            double prix = Double.parseDouble(prixField.getText());
            if (prix <= 0 || prix > 800) {
                errors.append("- Le prix doit être entre 0 et 800 DT\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Le prix doit être un nombre valide\n");
        }

        // Validation du prix pièce
        try {
            int prixPiece = Integer.parseInt(prixPieceField.getText());
            if (prixPiece <= 0 || prixPiece > 700) {
                errors.append("- Le prix pièce doit être entre 0 et 700 DT\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Le prix pièce doit être un nombre entier valide\n");
        }

        if (errors.length() > 0) {
            showAlert("Erreur de validation", "Veuillez corriger les erreurs suivantes", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
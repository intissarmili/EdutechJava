package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Certification;
import services.CertificationService;

import java.sql.SQLException;

public class modifierCertificationController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField prixField;
    @FXML private TextField imgField;
    @FXML private Button cancelBtn;
    @FXML private Button updateBtn;

    private CertificationService certificationService;
    private Certification certification;

    @FXML
    public void initialize() {
        certificationService = new CertificationService();
    }

    public void setCertificationData(Certification certification) {
        this.certification = certification;
        nomField.setText(certification.getNom());
        descriptionField.setText(certification.getDescription());
        prixField.setText(String.valueOf(certification.getPrix()));
        imgField.setText(certification.getImg());
    }

    @FXML
    private void handleUpdateCertification() {
        if (!validateInputs()) {
            return;
        }

        try {
            certification.setNom(nomField.getText());
            certification.setDescription(descriptionField.getText());
            certification.setPrix(Double.parseDouble(prixField.getText()));
            certification.setImg(imgField.getText());

            certificationService.update(certification);
            showAlert("Succès", "Modification réussie", "La certification a été modifiée avec succès.", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification", e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format invalide", "Veuillez entrer une valeur numérique valide pour le prix.", Alert.AlertType.ERROR);
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
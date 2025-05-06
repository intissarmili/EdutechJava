package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Cours;
import service.CoursService;
import service.CertificationService;
import java.sql.SQLException;

public class ModifierCoursController {

    // Correspond aux fx:id de votre FXML
    @FXML private TextField titreM;
    @FXML private TextField contenuM;
    @FXML private TextField categorieM;
    @FXML private ComboBox<Integer> idCertifM;

    private Cours coursToEdit;
    private CoursService coursService = new CoursService();
    private CertificationService certifService = new CertificationService();

    // Méthode appelée par le contrôleur parent pour passer le cours à modifier
    public void setCoursToEdit(Cours cours) {
        this.coursToEdit = cours;
        populateFields();
    }

    private void populateFields() {
        if (coursToEdit != null) {
            titreM.setText(coursToEdit.getTitre());
            contenuM.setText(coursToEdit.getContenu());
            categorieM.setText(coursToEdit.getCategorie());

            // Charger les IDs de certification disponibles
            try {
                idCertifM.getItems().addAll(certifService.getAvailableCertificationIds());
                idCertifM.getSelectionModel().select(coursToEdit.getCertificationId());
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de charger les certifications", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void ButtonConfirmerM() {
        if (validateFields()) {
            coursToEdit.setTitre(titreM.getText());
            coursToEdit.setContenu(contenuM.getText());
            coursToEdit.setCategorie(categorieM.getText());
            coursToEdit.setCertificationId(idCertifM.getValue());

            coursService.update(coursToEdit);
            showAlert("Succès", "Cours modifié avec succès", Alert.AlertType.INFORMATION);
            closeWindow();
        }
    }

    @FXML
    private void ButtonAcceuil() {
        closeWindow();
    }

    private boolean validateFields() {
        // Implémentez vos règles de validation ici
        return true;
    }

    private void closeWindow() {
        titreM.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package controllers.Back;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Cours;
import services.CertificationService;
import services.CoursService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class addCoursController {
    @FXML private TextField titreTextField;
    @FXML private TextArea contenuArea;
    @FXML private TextField categorieTextField;
    @FXML private ComboBox<Integer> idCertificationId;
    @FXML private Label pdfLabel;
    @FXML private Button cancelBtn;

    private File selectedPdfFile;
    private CoursService coursService = new CoursService();
    private CertificationService certificationService = new CertificationService();

    @FXML
    public void initialize() {
        loadAvailableCertificationIds();
    }

    private void loadAvailableCertificationIds() {
        try {
            List<Integer> availableIds = certificationService.getAvailableCertificationIds();
            idCertificationId.setItems(FXCollections.observableArrayList(availableIds));
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les certifications disponibles", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void selectPdfAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        selectedPdfFile = fileChooser.showOpenDialog(pdfLabel.getScene().getWindow());
        if (selectedPdfFile != null) {
            pdfLabel.setText(selectedPdfFile.getName());
        } else {
            pdfLabel.setText("Aucun fichier sélectionné");
        }
    }

    @FXML
    void ajouterCoursAction(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        Cours cours = new Cours(
                titreTextField.getText(),
                contenuArea.getText(),
                categorieTextField.getText(),
                idCertificationId.getValue()
        );

        if (selectedPdfFile != null) {
            cours.setContenu(selectedPdfFile.getName());
        }

        try {
            coursService.create(cours);
            showAlert("Succès", "Cours ajouté avec succès", Alert.AlertType.INFORMATION);
            redirectToCoursList();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'ajout du cours: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la redirection: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        String titre = titreTextField.getText();
        if (titre.isEmpty()) {
            showAlert("Erreur", "Le titre ne peut pas être vide", Alert.AlertType.ERROR);
            return false;
        }
        if (titre.length() < 5) {
            showAlert("Erreur", "Le titre doit contenir au moins 5 caractères", Alert.AlertType.ERROR);
            return false;
        }
        if (Character.isDigit(titre.charAt(0))) {
            showAlert("Erreur", "Le titre ne peut pas commencer par un chiffre", Alert.AlertType.ERROR);
            return false;
        }

        String categorie = categorieTextField.getText();
        if (categorie.isEmpty()) {
            showAlert("Erreur", "La catégorie ne peut pas être vide", Alert.AlertType.ERROR);
            return false;
        }
        if (categorie.length() < 5) {
            showAlert("Erreur", "La catégorie doit contenir au moins 5 caractères", Alert.AlertType.ERROR);
            return false;
        }

        if (idCertificationId.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un ID de certification valide", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleCancel() {
        closeWindow();
    }
    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void redirectToCoursList() throws IOException {
        // Fermer la fenêtre actuelle
        Stage currentStage = (Stage) titreTextField.getScene().getWindow();
        currentStage.close();

        // Ouvrir la fenêtre AffichierCours.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Back/AffichierCours.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Liste des Cours");
        stage.show();
    }
}
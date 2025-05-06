package controllers;
import javafx.scene.control.Alert;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.Node;

import java.io.IOException;

public class NavbarController {
    // Méthodes de navigation protégées pour permettre l'override
    @FXML
    protected void naviguerVersBlogCours(ActionEvent event) {
        chargerPage("/BlogCours.fxml", event);
    }

    @FXML
    protected void naviguerVersAffichierCours(ActionEvent event) {
        chargerPage("/AffichierCours.fxml", event);
    }

    @FXML
    protected void naviguerVersAffichierCertificat(ActionEvent event) {
        chargerPage("/AffichierCertification.fxml", event);
    }



    @FXML
    public void naviguerVersAffichierQuiz(ActionEvent event) {
        try {
            // Chemin relatif depuis la racine des ressources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierQuiz.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene currentScene = ((Node)event.getSource()).getScene();
            currentScene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page Quiz: " + e.getMessage());
        }
    }
    @FXML
    protected void naviguerVersAffichierQuestion(ActionEvent event) {
        chargerPage("/AffichierQuestion.fxml", event);
    }

    @FXML
    protected void naviguerVersAjoutCours(ActionEvent event) {
        chargerPage("/AjouterCours.fxml", event);
    }

    // Méthode générique pour charger les pages
    protected void chargerPage(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            System.err.println("Erreur de navigation: " + ex.getMessage());
            ex.printStackTrace();
        }
    }



    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
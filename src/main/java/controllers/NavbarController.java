package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
    protected void naviguerVersAffichierQuiz(ActionEvent event) {
        chargerPage("/AffichierQuiz.fxml", event);
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
}
package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.Window;

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
    protected void naviguerVersFeed(ActionEvent event) {
        chargerPage("/feed_commentaire.fxml", event);
    }
    @FXML
    protected void naviguerVersAvaibility(ActionEvent event) {
        chargerPage("/avaibility/listCards.fxml", event);
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

            // Obtenir la fenêtre actuelle quelle que soit la source de l'événement
            Stage stage = getStageFromEvent(event);

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                System.err.println("Impossible de trouver la fenêtre pour charger la nouvelle scène");
            }
        } catch (IOException ex) {
            System.err.println("Erreur de navigation: " + ex.getMessage());
            ex.printStackTrace();
        }
    }






















    // Méthode pour obtenir la Stage quelle que soit la source de l'événement
    private Stage getStageFromEvent(ActionEvent event) {
        Object source = event.getSource();

        // Si la source est un Node, on obtient la fenêtre directement
        if (source instanceof javafx.scene.Node) {
            return (Stage) ((javafx.scene.Node) source).getScene().getWindow();
        }
        // Si la source est un MenuItem, on cherche la fenêtre active
        else if (source instanceof MenuItem) {
            // Option 1: Obtenir la fenêtre active (focus)
            return (Stage) javafx.stage.Stage.getWindows().stream()
                    .filter(Window::isFocused)
                    .findFirst()
                    .orElse(null);

            // Option 2 (alternative): Si vous avez accès à un Node de référence dans votre classe
            // return (Stage) votreNodeDeReference.getScene().getWindow();
        }

        return null;
    }
}
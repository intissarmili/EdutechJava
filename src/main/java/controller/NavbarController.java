package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavbarController {

    @FXML
    public void naviguerVersAffichierevent(ActionEvent event) {
        try {
            System.out.println("Tentative de navigation vers la liste des événements...");

            // Chargement de la vue ListeEvent.fxml avec chemin précis
            String fxmlPath = "/vue/ListeEvent.fxml";
            System.out.println("Chargement du fichier FXML: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Obtenir la scène actuelle
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Créer une nouvelle scène avec la vue ListeEvent
            Scene scene = new Scene(root);

            // Remplacer la scène actuelle
            stage.setScene(scene);
            stage.setTitle("Gestion des Événements");
            stage.show();

            System.out.println("Navigation vers la liste des événements réussie!");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue ListeEvent.fxml : " + e.getMessage());
            e.printStackTrace();
            // Afficher une alerte à l'utilisateur
            showErrorAlert("Erreur de navigation", "Impossible de charger la page des événements", e.getMessage());
        }
    }

    // Pour naviguer vers l'ajout d'événement
    @FXML
    public void naviguerVersAjouterEvent(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/Ajouterevent.fxml"));
            Parent root = loader.load();

            // Plutôt que d'ouvrir une nouvelle fenêtre, remplacer la scène actuelle
            // pour une expérience de navigation plus cohérente
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ajouter un événement");
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Ajouterevent.fxml : " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de navigation", "Impossible de charger la page d'ajout d'événement", e.getMessage());
        }
    }

    // Méthode d'affichage d'alerte d'erreur
    private void showErrorAlert(String title, String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
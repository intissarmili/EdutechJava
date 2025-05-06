package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import models.Abonnement;
import services.AbonnementService;

import java.io.IOException;

public class AjouterAbonnementController {

    @FXML
    private TextField nomField, prixField, dureeField, descriptionField;

    private final AbonnementService abonnementService = new AbonnementService();


    public void initialize() {
        // Validation pour le champ "prix" en utilisant un DoubleStringConverter
        prixField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));

        // Validation pour le champ "duree" en utilisant un IntegerStringConverter
        dureeField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
    }

    @FXML
    public void handleAjouterAbonnement() {
        String nom = nomField.getText().trim();
        String prix = prixField.getText().trim();
        String duree = dureeField.getText().trim();
        String description = descriptionField.getText().trim();

        if (nom.isEmpty() || prix.isEmpty() || duree.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        try {
            // Ajouter l'abonnement
            Abonnement abonnement = new Abonnement(nom, duree, prix, description);
            abonnementService.addAbonnement(abonnement);

            // Charger la vue ListeAbonnements
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficher-abonnements.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur pour rafraîchir la liste
            ListeAbonnementController controller = loader.getController();
            controller.initialize(); // Recharger la table avec la nouvelle liste

            // Remplacer la scène
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));

            showAlert("Succès", "Abonnement ajouté avec succès.");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix et la durée doivent être des nombres valides.");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue des abonnements.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

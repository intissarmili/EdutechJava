package controller;

import controller.FavoriteListController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import models.Certification;
import services.CertificationService;
import services.PanierService;
import services.FavoriteService;
import services.PointsService; // Important à ajouter

import java.io.IOException;
import java.util.List;

public class AfficherCertificationsController {


    @FXML
    private FlowPane certifContainer;


    @FXML
    private Label soldeLabel; // Label pour afficher le solde

    private final CertificationService certificationService = new CertificationService();
    private final PanierService panierService = new PanierService();
    private final FavoriteService favoriteService = new FavoriteService();
    private final PointsService pointsService = new PointsService(); // Instancier le service des points

    private final int userId = 1;
    private double soldePoints; // Stocker le solde en mémoire

    @FXML
    private Button payerButton; // Déclare un bouton pour le paiement

    @FXML
    public void initialize() {
        // Ajoute la logique existante pour initialiser les certifications
        List<Certification> certifications = certificationService.getAll();
        soldePoints = pointsService.getUserPoints(userId); // Charger le solde
        updateSoldeLabel((int) soldePoints);

        for (Certification certif : certifications) {
            VBox card = createCertificationCard(certif);
            certifContainer.getChildren().add(card);
        }
    }

    // Bouton pour accéder à la vue du panier
    @FXML
    private void handlePayer(javafx.event.ActionEvent event) {
        try {
            Parent panierView = FXMLLoader.load(getClass().getResource("/PanierView.fxml"));
            Scene panierScene = new Scene(panierView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(panierScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private VBox createCertificationCard(Certification certif) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 10; -fx-padding: 15; -fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        card.setPrefWidth(200);

        String imagePath = "/images/" + certif.getImg();
        Image image;
        try {
            image = new Image(getClass().getResource(imagePath).toExternalForm());
        } catch (Exception e) {
            image = new Image(getClass().getResource("/images/img.png").toExternalForm());
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        Label nomLabel = new Label(certif.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(certif.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px;");

        Label prixLabel = new Label(certif.getPrix() + " points"); // Afficher en points

        prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        Button btnAjouter = new Button("Ajouter au panier");
        btnAjouter.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white;");
        btnAjouter.setOnAction(event -> {
            if (soldePoints >= certif.getPrix()) {  // Comparer aux POINTS RESTANTS !!!
                panierService.ajouterCertification(certif);
                pointsService.deduirePoints(userId, certif.getPrix());
                soldePoints -= certif.getPrix(); // Mettre à jour le solde local !
                updateSoldeLabel((int) soldePoints);

                afficherPanier(event); // Aller direct au panier
            } else {
                showAlert("Points insuffisants", "Vous n'avez pas assez de points restants pour acheter cette certification !");
            }
        });


        Button heartBtn = new Button();
        heartBtn.setText(favoriteService.isFavorite(userId, certif.getId()) ? "♥" : "♡");
        updateHeartColor(heartBtn);

        heartBtn.setOnAction(event -> {
            if (!favoriteService.isFavorite(userId, certif.getId())) {
                favoriteService.addFavorite(userId, certif.getId());
                heartBtn.setText("♥");
                updateHeartColor(heartBtn);
                ouvrirListeFavoris(certif, event);
            } else {
                favoriteService.removeFavorite(userId, certif.getId());
                heartBtn.setText("♡");
                updateHeartColor(heartBtn);
            }
        });

        card.getChildren().addAll(imageView, nomLabel, descriptionLabel, prixLabel, btnAjouter, heartBtn);
        return card;
    }

    private void updateHeartColor(Button heartBtn) {
        if ("♥".equals(heartBtn.getText())) {
            heartBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: red;");
        } else {
            heartBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: black;");
        }
    }

    private void afficherPanier(javafx.event.ActionEvent event) {
        try {
            Parent panierView = FXMLLoader.load(getClass().getResource("/PanierView.fxml"));
            Scene panierScene = new Scene(panierView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(panierScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirListeFavoris(Certification certification, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FavoriteListView.fxml"));
            Parent favoriteView = loader.load();

            Scene scene = new Scene(favoriteView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void updateSoldeLabel(int solde) {
        soldeLabel.setText("Votre solde actuel : " + solde + " points");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
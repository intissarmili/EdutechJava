package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.Certification;
import services.CertificationService;
import services.FavoriteService;
import models.Favorite;

import java.util.List;

public class FavoriteListController {

    @FXML
    private FlowPane favoriteContainer;

    private final FavoriteService favoriteService = new FavoriteService();
    private final CertificationService certificationService = new CertificationService();
    private final int userId = 1; // Ton utilisateur connecté

    @FXML
    public void initialize() {
        afficherFavoris();
    }

    private void afficherFavoris() {
        favoriteContainer.getChildren().clear(); // On vide d'abord le conteneur pour éviter les doublons

        List<Favorite> favorites = favoriteService.getFavoritesByUserId(userId);

        if (favorites.isEmpty()) {
            Label emptyMessage = new Label("Aucun favori trouvé.");
            emptyMessage.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
            favoriteContainer.getChildren().add(emptyMessage);
        } else {
            for (Favorite fav : favorites) {
                Certification certif = certificationService.getById(fav.getCertificationId());
                if (certif != null) {
                    VBox card = createFavoriteCard(certif);
                    favoriteContainer.getChildren().add(card);
                }
            }
        }
    }


    private VBox createFavoriteCard(Certification certif) {
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

        Label prixLabel = new Label(certif.getPrix() + " points");
        prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        // AJOUT DU COEUR ❤️
        Button heartBtn = new Button("♥");
        heartBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-text-fill: red;");

        heartBtn.setOnAction(event -> {
            favoriteService.removeFavorite(userId, certif.getId());
            afficherFavoris(); // Recharger la liste des favoris après suppression
        });

        card.getChildren().addAll(imageView, nomLabel, descriptionLabel, prixLabel, heartBtn);

        return card;
    }
}

package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import models.Certification;
import models.Favorite;
import service.CertificationService;
import services.FavoriteService;

import java.sql.SQLException;
import java.util.List;

public class AfficherFavoritesController {

    @FXML
    private FlowPane favoriteContainer;

    private final CertificationService certificationService = new CertificationService();
    private final FavoriteService favoriteService = new FavoriteService();

    private final int userId = 1; // ID de l'utilisateur actuel, à ajuster

    @FXML
    public void initialize() throws SQLException {
        // Récupérer tous les favoris de l'utilisateur
        List<Favorite> favorites = favoriteService.getFavoritesByUserId(userId);

        // Récupérer les certifications à partir des favoris
        for (Favorite favorite : favorites) {
            Certification certif = certificationService.getById(favorite.getCertificationId());
            // Crée une carte pour chaque certification favorite
            favoriteContainer.getChildren().add(createCertificationCard(certif));
        }
    }

    private VBox createCertificationCard(Certification certif) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 10; -fx-padding: 15; -fx-background-color: #f9f9f9; -fx-background-radius: 10;");
        card.setPrefWidth(200);

        // Charger l'image
        String imagePath = "/images/" + certif.getImg(); // Assurez-vous que le chemin est correct
        Image image = new Image(getClass().getResource(imagePath).toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        // Informations sur la certification
        Label nomLabel = new Label(certif.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(certif.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px;");

        Label prixLabel = new Label(certif.getPrix() + " DT");
        prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        // Icône de cœur pour ajouter aux favoris
        ImageView heartIcon = new ImageView(new Image(getClass().getResource("/images/heart.png").toExternalForm()));
        heartIcon.setFitWidth(20);
        heartIcon.setFitHeight(20);
        heartIcon.setOnMouseClicked(event -> toggleFavorite(certif, heartIcon));

        // Ajouter les éléments à la carte
        card.getChildren().addAll(imageView, nomLabel, descriptionLabel, prixLabel, heartIcon);
        return card;
    }

    private void toggleFavorite(Certification certif, ImageView heartIcon) {
        // Vérifier si la certification est déjà dans les favoris
        boolean isFavorite = favoriteService.isFavorite(userId, certif.getId());

        if (isFavorite) {
            // Retirer des favoris
            favoriteService.removeFavorite(userId, certif.getId());
            heartIcon.setImage(new Image(getClass().getResource("/images/heart_empty.png").toExternalForm())); // Changer l'icône
        } else {
            // Ajouter aux favoris
            favoriteService.addFavorite(userId, certif.getId());
            heartIcon.setImage(new Image(getClass().getResource("/images/heart_filled.png").toExternalForm())); // Changer l'icône
        }
    }
}

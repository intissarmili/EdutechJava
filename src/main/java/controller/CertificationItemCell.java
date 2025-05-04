package controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import models.Certification;
import services.FavoriteService;
import services.PanierService;
import utils.MaConnexion;

public class CertificationItemCell extends ListCell<Certification> {

    private HBox content;
    private ImageView imageView;
    private VBox details;
    private Label nomLabel;
    private Label descriptionLabel;
    private Label prixLabel;
    private Button addButton;
    private Button heartButton;

    private FavoriteService favoriteService;
    private PanierService panierService;

    private final int userId = 1; // à adapter avec l'utilisateur connecté

    public CertificationItemCell() {
        super();

        // Services
        // No need to pass the connection since it's handled inside the service class
        FavoriteService favoriteService = new FavoriteService();
        panierService = new PanierService(MaConnexion.getInstance().getConn());

        imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);

        nomLabel = new Label();
        nomLabel.setFont(new Font("Arial", 16));

        descriptionLabel = new Label();
        prixLabel = new Label();

        // Bouton panier
        addButton = new Button("Ajouter au panier");
        addButton.setOnAction(event -> {
            Certification certif = getItem();
            if (certif != null) {
                panierService.ajouterCertification(certif);
                System.out.println("Ajoutée au panier : " + certif.getNom());
            }
        });

        // Bouton favoris (cœur)
        heartButton = new Button("♡");
        heartButton.setFont(new Font("Arial", 18));
        heartButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
        heartButton.setOnAction(event -> {
            Certification certif = getItem();
            if (certif != null) {
                if (favoriteService.isFavorite(userId, certif.getId())) {
                    favoriteService.removeFavorite(userId, certif.getId());
                    heartButton.setText("♡");
                    System.out.println("Retirée des favoris : " + certif.getNom());
                } else {
                    favoriteService.addFavorite(userId, certif.getId());
                    heartButton.setText("♥");
                    System.out.println("Ajoutée aux favoris : " + certif.getNom());
                }
            }
        });

        details = new VBox(nomLabel, descriptionLabel, prixLabel, new HBox(10, addButton, heartButton));
        details.setSpacing(5);

        content = new HBox(15, imageView, details);
        content.setPadding(new Insets(10));
    }

    @Override
    protected void updateItem(Certification item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setGraphic(null);
        } else {
            nomLabel.setText(item.getNom());
            descriptionLabel.setText(item.getDescription());
            prixLabel.setText("Prix : " + item.getPrix() + " TND");

            try {
                Image img = new Image(item.getImg(), true);
                imageView.setImage(img);
            } catch (Exception e) {
                imageView.setImage(null);
            }

            // Mettre à jour l'état du cœur
            heartButton.setText(favoriteService.isFavorite(userId, item.getId()) ? "♥" : "♡");

            setGraphic(content);
        }
    }
}

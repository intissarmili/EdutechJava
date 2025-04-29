package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Cours;
import service.CoursService;
import java.util.Set;

import java.io.InputStream;
import java.io.FileNotFoundException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
// ... autres imports existants ...
import java.io.IOException;
import java.util.List;

public class BlogCoursController {

    @FXML
    private VBox coursContainer;
    @FXML
    private VBox templateCours;
    @FXML
    private ImageView templateImage;
    @FXML
    private Label templateCategorie;
    @FXML
    private Label templateTitre;

    private final CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        templateCours.setVisible(false); // Cache le template
        loadCours();
    }

    private void loadCours() {
        try {
            List<Cours> coursList = coursService.readAll();
            for (Cours cours : coursList) {
                coursContainer.getChildren().add(createCoursItem(cours));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger les cours");
        }
    }

    private VBox createCoursItem(Cours cours) {
        // Clone le template
        VBox coursItem = new VBox();
        coursItem.setSpacing(templateCours.getSpacing());
        coursItem.setStyle(templateCours.getStyle());

        // Configure l'image
        ImageView coursImage = new ImageView();
        configureCourseImage(coursImage, cours.getContenu());

        // Configure les labels
        Label categorieLabel = new Label("Catégorie: " + cours.getCategorie());
        categorieLabel.setStyle(templateCategorie.getStyle());

        Label titreLabel = new Label(cours.getTitre());
        titreLabel.setStyle(templateTitre.getStyle());
        configureTitleHoverEffect(titreLabel, cours);

        coursItem.getChildren().addAll(coursImage, categorieLabel, titreLabel);
        return coursItem;
    }

    private void configureCourseImage(ImageView imageView, String imageName) {
        // Liste des images valides
        Set<String> validImages = Set.of(
                "imageCours1.png",
                "imageCours2.png",
                "imageCours3.png"
        );

        // Normalise le nom du fichier
        String normalizedName = imageName != null ? imageName.trim() : "";

        if (!validImages.contains(normalizedName)) {
            normalizedName = "default-course.png"; // Fallback sur l'image par défaut
        }

        try {
            String imagePath = "/assets/" + normalizedName;
            InputStream inputStream = getClass().getResourceAsStream(imagePath);

            if (inputStream == null) {
                // Si même l'image par défaut n'existe pas
                System.err.println("Image manquante: " + imagePath);
                return;
            }

            Image image = new Image(inputStream);
            imageView.setImage(image);
            imageView.setFitHeight(200);
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);

        } catch (Exception e) {
            System.err.println("Erreur critique de chargement d'image: " + e.getMessage());
        }
    }

    private void configureTitleHoverEffect(Label label, Cours cours) {
        label.setOnMouseEntered(e -> label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db;"));
        label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;"));
        label.setOnMouseClicked((MouseEvent event) -> navigateToDetails(cours, label));
    }

    private void navigateToDetails(Cours cours, Label sourceLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetails.fxml"));
            Parent root = loader.load();
            BlogDetailsController detailsController = loader.getController();
            detailsController.setCours(cours);

            Stage stage = (Stage) sourceLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1024, 768));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir les détails du cours");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
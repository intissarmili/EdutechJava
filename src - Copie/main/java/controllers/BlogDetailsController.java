package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Cours;
import models.Quiz;
import models.Certification;
import services.QuizService;
import services.CertificationService;

import java.sql.SQLException;
import java.util.List;

public class BlogDetailsController {

    @FXML private ImageView coursImage;
    @FXML private Label titreLabel;
    @FXML private Label categorieLabel;
    @FXML private ListView<String> quizList;
    @FXML private Label certificationLabel;

    private final QuizService quizService = new QuizService();
    private final CertificationService certificationService = new CertificationService();

    public void setCours(Cours cours) {
        try {
            // Configuration des éléments de base
            titreLabel.setText(cours.getTitre());
            categorieLabel.setText("Catégorie: " + cours.getCategorie());

            // Style des labels
            titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            categorieLabel.setStyle("-fx-text-fill: #16ac63; -fx-font-size: 18px; -fx-font-weight: bold;");

            // Effet hover sur le titre
            titreLabel.setOnMouseEntered(e -> titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3498db;"));
            titreLabel.setOnMouseExited(e -> titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;"));

            // Chargement de l'image
            try {
                Image image = new Image("file:assets/" + cours.getContenu());
                coursImage.setImage(image);
                coursImage.setPreserveRatio(true);
            } catch (Exception e) {
                coursImage.setImage(new Image(getClass().getResourceAsStream("/assets/default-course.png")));
            }

            // Chargement des quizzes
            quizList.getItems().clear();
            List<Quiz> quizzes = quizService.findByCoursId(cours.getId());
            for (Quiz quiz : quizzes) {
                quizList.getItems().add("Quiz ID: " + quiz.getId());
            }

            // Certification
            Certification certif = certificationService.findById(cours.getCertificationId());
            if (certif != null) {
                certificationLabel.setText("Certification ID: " + certif.getId());
                certificationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            } else {
                certificationLabel.setText("Aucune certification associée");
                certificationLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Problème lors du chargement des données du cours");
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
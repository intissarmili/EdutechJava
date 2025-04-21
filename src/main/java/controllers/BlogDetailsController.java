package controllers;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import models.Cours;
import models.Quiz;
import models.Certification;
import services.QuizService;
import services.CertificationService;
import java.sql.SQLException;
import java.util.List;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
public class BlogDetailsController {

    @FXML private ImageView coursImage;
    @FXML private Label titreLabel;
    @FXML private Label categorieLabel;
    @FXML private Label descriptionLabel;
    @FXML private ListView<String> quizList;
    @FXML private Label certificationLabel;
    private Cours currentCours; // Ajoutez cette variable de classe

    private final QuizService quizService = new QuizService();
    private final CertificationService certificationService = new CertificationService();

    public void setCours(Cours cours) {
        this.currentCours = cours; // Stockez le cours courant

        try {
            // Description d’introduction
            descriptionLabel.setText("""
                Pour renforcer vos connaissances, nous proposons également des quiz interactifs 
                qui vous permettront de tester vos acquis et d'évaluer votre progression.
                À la fin de chaque module, vous aurez la possibilité d'obtenir une certification, 
                attestant de vos compétences et de votre engagement à vous améliorer.
            """);
            descriptionLabel.setWrapText(true);
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

            // Titre et catégorie
            titreLabel.setText(cours.getTitre());
            categorieLabel.setText("Catégorie: " + cours.getCategorie());
            titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            categorieLabel.setStyle("-fx-text-fill: #16ac63; -fx-font-size: 18px; -fx-font-weight: bold;");
            titreLabel.setOnMouseEntered(e -> titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3498db;"));
            titreLabel.setOnMouseExited(e -> titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;"));

            // Image du cours
            try {
                Image image = new Image("file:assets/" + cours.getContenu());
                coursImage.setImage(image);
                coursImage.setPreserveRatio(true);
            } catch (Exception e) {
                coursImage.setImage(new Image(getClass().getResourceAsStream("/assets/default-course.png")));
            }

            // Liste des Quiz
            quizList.getItems().clear();
            List<Quiz> quizzes = quizService.findByCoursId(cours.getId());
            for (Quiz quiz : quizzes) {
                quizList.getItems().add("Quiz ID: " + quiz.getId());
            }

            // Clic sur un quiz
            quizList.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    String selected = quizList.getSelectionModel().getSelectedItem();
                    if (selected != null && selected.startsWith("Quiz ID: ")) {
                        int quizId = Integer.parseInt(selected.replace("Quiz ID: ", "").trim());
                        playQuiz(quizId);
                    }
                }
            });

            // Certification
            Certification certif = certificationService.findById(cours.getCertificationId());
            if (certif != null) {
                certificationLabel.setText("Certification ID: " + certif.getId());
                certificationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                certificationLabel.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                        playCertification(certif.getId());
                    }
                });
                certificationLabel.setOnMouseEntered(e -> certificationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2980b9;"));
                certificationLabel.setOnMouseExited(e -> certificationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"));
            } else {
                certificationLabel.setText("Aucune certification associée");
                certificationLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic;");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Problème lors du chargement des données du cours");
        }
    }

    // Ajoutez cette nouvelle méthode pour rafraîchir la liste
    public void refreshQuizList() {
        if (currentCours != null) {
            try {
                quizList.getItems().clear();
                List<Quiz> quizzes = quizService.findByCoursId(currentCours.getId());
                for (Quiz quiz : quizzes) {
                    quizList.getItems().add("Quiz ID: " + quiz.getId());
                }
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de rafraîchir la liste des quiz: " + e.getMessage());
            }
        }
    }

    private void playCertification(int certifId) {
        // ⚠️ À implémenter : ouvrir la certification
        System.out.println("Afficher la certification avec l'ID: " + certifId);
        // Exemple :
        // CertificationController.launchCertificationById(certifId);
    }



    private void playQuiz(int quizId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/playQuiz.fxml"));
            Parent root = loader.load();

            QuizView quizController = loader.getController();
            quizController.initializeQuiz(quizId);

            Stage stage = new Stage();
            stage.setTitle("Quiz Interactif");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger l'interface du quiz : " + e.getMessage());
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

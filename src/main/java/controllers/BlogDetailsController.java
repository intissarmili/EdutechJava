package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import models.Certification;
import models.Cours;
import models.Quiz;
import service.CertificationService;
import service.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class BlogDetailsController {

    @FXML private ImageView coursImage;
    @FXML private Label titreLabel;
    @FXML private Label categorieLabel;
    @FXML private Label descriptionLabel;
    @FXML private ListView<String> quizList;
    @FXML private Label certificationLabel;

    private final QuizService quizService = new QuizService();
    private final CertificationService certificationService = new CertificationService();
    private Cours currentCours;

    public void setCours(Cours cours) {
        this.currentCours = cours;
        try {
            setupCourseDetails(cours);
            loadCourseImage(cours.getContenu());
            loadQuizList(cours.getId());
            loadCertification(cours.getCertificationId());
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Erreur lors du chargement des données du cours.");
        }
    }

    private void setupCourseDetails(Cours cours) {
        descriptionLabel.setText("""
            Pour renforcer vos connaissances, nous proposons également des quiz interactifs 
            qui vous permettront de tester vos acquis et d'évaluer votre progression.
            À la fin de chaque module, vous aurez la possibilité d'obtenir une certification, 
            attestant de vos compétences et de votre engagement à vous améliorer.
        """);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        titreLabel.setText(cours.getTitre());
        titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titreLabel.setOnMouseEntered(e -> titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3498db;"));
        titreLabel.setOnMouseExited(e -> titreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;"));

        categorieLabel.setText("Catégorie: " + cours.getCategorie());
        categorieLabel.setStyle("-fx-text-fill: #16ac63; -fx-font-size: 18px; -fx-font-weight: bold;");
    }

    private void loadCourseImage(String imageName) {
        try {
            Image image = new Image("file:assets/" + imageName);
            coursImage.setImage(image);
        } catch (Exception e) {
            coursImage.setImage(new Image(getClass().getResourceAsStream("/assets/default-course.png")));
        }
        coursImage.setPreserveRatio(true);
    }

    private void loadQuizList(int coursId) throws SQLException {
        quizList.getItems().clear();
        List<Quiz> quizzes = quizService.findByCoursId(coursId);
        for (Quiz quiz : quizzes) {
            quizList.getItems().add("Quiz ID: " + quiz.getId());
        }

        quizList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                String selected = quizList.getSelectionModel().getSelectedItem();
                if (selected != null && selected.startsWith("Quiz ID: ")) {
                    int quizId = Integer.parseInt(selected.replace("Quiz ID: ", "").trim());
                    playQuiz(quizId);
                }
            }
        });
    }

    private void loadCertification(int certificationId) throws SQLException {
        Certification certif = certificationService.findById(certificationId);
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
    }

    public void refreshQuizList() {
        if (currentCours != null) {
            try {
                loadQuizList(currentCours.getId());
            } catch (SQLException e) {
                showErrorAlert("Erreur", "Impossible de rafraîchir la liste des quiz: " + e.getMessage());
            }
        }
    }

    private void playCertification(int certificationId) {
        // TODO: Implémenter l'affichage de la certification
        System.out.println("Afficher la certification avec l'ID: " + certificationId);
    }

    private void playQuiz(int quizId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/playQuiz.fxml"));
            Parent quizView = loader.load();

            QuizView quizController = loader.getController();
            quizController.initializeQuiz(quizId);

            Stage stage = new Stage();
            stage.setScene(new Scene(quizView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger le quiz.");
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

package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Quiz;
import service.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class QuizStatisticsController {
    @FXML private PieChart quizPieChart;
    @FXML private Label successRateLabel;
    @FXML private Label failureRateLabel;
    @FXML private Label totalQuizzesLabel;
    @FXML private Label passedQuizzesLabel;
    @FXML private Label failedQuizzesLabel;

    private final QuizService quizService = new QuizService();

    @FXML
    public void initialize() {
        try {
            loadStatistics();
            animateChart();
        } catch (Exception e) {
            showAlert("Erreur d'initialisation", "Échec du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try {
            List<Quiz> quizzes = quizService.readAll();

            if (quizzes == null || quizzes.isEmpty()) {
                showAlert("Aucune donnée", "Aucun quiz disponible pour les statistiques");
                return;
            }

            // Déclarer les variables comme final
            final int totalQuiz = quizzes.size();
            int quizReussis = 0;

            for (Quiz quiz : quizzes) {
                if (quiz.estReussi()) {
                    quizReussis++;
                }
            }

            final double tauxReussite = (totalQuiz > 0) ? ((double) quizReussis / totalQuiz) * 100 : 0;
            final double tauxEchec = 100 - tauxReussite;
            final int finalQuizReussis = quizReussis; // Variable effectivement finale

            Platform.runLater(() -> {
                updateUI(tauxReussite, tauxEchec, totalQuiz, finalQuizReussis);
            });

        } catch (SQLException e) {
            showAlert("Erreur SQL", "Échec de la lecture des quizzes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUI(double tauxReussite, double tauxEchec, int totalQuiz, int quizReussis) {
        successRateLabel.setText(String.format("Taux de réussite: %.2f%%", tauxReussite));
        failureRateLabel.setText(String.format("Taux d'échec: %.2f%%", tauxEchec));
        totalQuizzesLabel.setText(String.format("Total quizzes: %d", totalQuiz));
        passedQuizzesLabel.setText(String.format("Quizzes réussis: %d", quizReussis));
        failedQuizzesLabel.setText(String.format("Quizzes échoués: %d", totalQuiz - quizReussis));

        quizPieChart.getData().clear();
        PieChart.Data successData = new PieChart.Data("Réussite", tauxReussite);
        PieChart.Data failureData = new PieChart.Data("Échec", tauxEchec);
        quizPieChart.getData().addAll(successData, failureData);

        successData.getNode().setStyle("-fx-pie-color: #4CAF50;");
        failureData.getNode().setStyle("-fx-pie-color: #F44336;");
    }

    private void animateChart() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), quizPieChart);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), quizPieChart);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(1);
        rotateTransition.play();
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/affichierQuiz.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur de navigation", "Échec du retour à la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.Question;
import models.Quiz;
import services.QuizService;

import java.sql.SQLException;
import java.util.List;

public class QuizView {

    @FXML private VBox quizContainer;
    @FXML private VBox resultsContainer;
    @FXML private Label labelQuestion;
    @FXML private VBox optionsBox;
    @FXML private Label noteLabel;
    @FXML private Label resultIcon;
    @FXML private Label scoreLabel;
    @FXML private Label coinsLabel;

    private List<Question> questions;
    private String[] userAnswers;
    private int currentIndex = 0;
    private Quiz quiz;

    public void initializeQuiz(int quizId) {
        try {
            QuizService quizService = new QuizService();
            this.questions = quizService.getQuestionsForQuiz(quizId);
            this.userAnswers = new String[questions.size()];
            this.quiz = new Quiz();
            quiz.setQuestions(questions);
            showQuestion(currentIndex);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des questions : " + e.getMessage());
        }
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        Question q = questions.get(index);
        labelQuestion.setText((index + 1) + ". " + q.getQuestion());
        optionsBox.getChildren().clear();
        ToggleGroup group = new ToggleGroup();

        for (String option : q.getDeserializedOptions()) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            if (userAnswers[index] != null && userAnswers[index].equals(option)) {
                rb.setSelected(true);
            }
            optionsBox.getChildren().add(rb);
        }

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                userAnswers[index] = ((RadioButton) newVal).getText();
            }
        });
    }

    @FXML
    private void handleNext() {
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion(currentIndex);
        }
    }

    @FXML
    private void handlePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            showQuestion(currentIndex);
        }
    }

    @FXML
    private void handleSubmit() {
        String[] reponsesArray = new String[userAnswers.length];
        System.arraycopy(userAnswers, 0, reponsesArray, 0, userAnswers.length);

        quiz.setReponseUtilisateur(reponsesArray);
        int score = quiz.calculerNote();
        quiz.setNote(score);

        showResults(score);
    }

    @FXML
    private void handleFinish() {
        // Fermer la fenêtre ou retourner à l'accueil
        noteLabel.getScene().getWindow().hide();
    }

    private void showResults(int score) {
        // Masquer le quiz et afficher les résultats
        quizContainer.setVisible(false);
        resultsContainer.setVisible(true);

        // Calcul des pièces (40 par bonne réponse)
        int coins = score * 40;

        // Affichage des résultats
        if (score > questions.size() / 2) {
            resultIcon.setText("🏆"); // Trophée pour réussite
            resultIcon.setStyle("-fx-text-fill: #28a745;");
        } else {
            resultIcon.setText("❌"); // Croix pour échec
            resultIcon.setStyle("-fx-text-fill: #dc3545;");
        }

        scoreLabel.setText("Votre note : " + score + " / " + questions.size());
        coinsLabel.setText("Pièces gagnées : " + coins + " 🪙");

        // Mise à jour du label de note
        noteLabel.setText("Quiz terminé !");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package controllers;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.Question;
import models.Quiz;
import service.QuizService;

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
        QuizService quizService = new QuizService();
        this.questions = quizService.getQuestionsForQuiz(quizId);
        this.userAnswers = new String[questions.size()];
        this.quiz = new Quiz();
        quiz.setQuestions(questions);
        showQuestion(currentIndex);
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        Question q = questions.get(index);
        labelQuestion.setText((index + 1) + ". " + q.getQuestion());
        optionsBox.getChildren().clear();

        ToggleGroup group = new ToggleGroup();
        q.getDeserializedOptions().forEach(option -> {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
            if (option.equals(userAnswers[index])) {
                rb.setSelected(true);
            }
            optionsBox.getChildren().add(rb);
        });

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
        quiz.setReponseUtilisateur(userAnswers.clone());
        int score = quiz.calculerNote();
        showResults(score);
    }

    @FXML
    private void handleFinish() {
        quizContainer.getScene().getWindow().hide();
    }

    private void showResults(int score) {
        quizContainer.setVisible(false);
        resultsContainer.setVisible(true);

        int coins = score * 40;
        resultIcon.setText(score > questions.size() / 2 ? "🏆" : "❌");
        resultIcon.setStyle(score > questions.size() / 2 ? "-fx-text-fill: #28a745;" : "-fx-text-fill: #dc3545;");

        scoreLabel.setText("Score: " + score + "/" + questions.size());
        coinsLabel.setText("Pièces gagnées: " + coins + " 🪙");
        noteLabel.setText("Quiz terminé!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
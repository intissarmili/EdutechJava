package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.util.Duration;
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
    @FXML private Label resultIcon;
    @FXML private Label scoreLabel;
    @FXML private Label coinsLabel;
    @FXML private ProgressBar timerProgress;
    @FXML private Label timerLabel;
    @FXML private Label noteLabel;

    private List<Question> questions;
    private String[] userAnswers;
    private int currentIndex = 0;
    private Quiz quiz;
    private Timeline timeline;
    private static final int TIME_LIMIT = 30;
    private int timeRemaining;

    public void initializeQuiz(int quizId) {
        try {
            QuizService quizService = new QuizService();
            this.questions = quizService.getQuestionsForQuiz(quizId);
            this.userAnswers = new String[questions.size()];
            this.quiz = new Quiz();
            quiz.setQuestions(questions);
            showQuestion(currentIndex); // timer démarrera dans showQuestion directement
        } catch (SQLException e) {
            showError("Erreur lors du chargement du quiz : " + e.getMessage());
        }
    }

    private void startTimer() {
        timeRemaining = TIME_LIMIT;
        updateTimerDisplay();

        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    timeRemaining--;
                    updateTimerDisplay();

                    if (timeRemaining <= 0) {
                        timeline.stop();
                        handleTimeExpired();
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimerDisplay() {
        timerLabel.setText(timeRemaining + "s");
        timerProgress.setProgress((double) timeRemaining / TIME_LIMIT);
    }

    private void handleTimeExpired() {
        String correctAnswer = questions.get(currentIndex).getReponseCorrecte();
        highlightAnswer(correctAnswer, true);
        userAnswers[currentIndex] = null; // Aucun choix = réponse fausse
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        if (timeline != null) {
            timeline.stop();
        }

        optionsBox.getChildren().clear();
        Question q = questions.get(index);
        labelQuestion.setText((index + 1) + ". " + q.getQuestion());

        ToggleGroup group = new ToggleGroup();
        for (String option : q.getDeserializedOptions()) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size: 14px; -fx-padding: 8 12;");

            if (userAnswers[index] != null && userAnswers[index].equals(option)) {
                rb.setSelected(true);
            }

            optionsBox.getChildren().add(rb);
        }

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String selectedAnswer = ((RadioButton) newVal).getText();
                userAnswers[index] = selectedAnswer;
                String correctAnswer = q.getReponseCorrecte();
                highlightAnswer(correctAnswer, false);
            }
        });

        startTimer(); // seulement ici
    }

    private void highlightAnswer(String correctAnswer, boolean timeExpired) {
        for (Node node : optionsBox.getChildren()) {
            if (node instanceof RadioButton rb) {
                if (rb.getText().equals(correctAnswer)) {
                    rb.setStyle("-fx-text-fill: #00cc00; -fx-font-weight: bold;");
                } else {
                    rb.setStyle("-fx-text-fill: black;"); // Important: reset style
                }
            }
        }
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
        if (timeline != null) {
            timeline.stop();
        }

        quiz.setReponseUtilisateur(userAnswers.clone());
        int score = quiz.calculerNote();
        showResults(score);
    }
    @FXML

    private void showResults(int score) {
        quizContainer.setVisible(false);
        resultsContainer.setVisible(true);

        int coins = score * 40;
        resultIcon.setText(score > questions.size() / 2 ? "🏆" : "❌");
        resultIcon.setStyle(score > questions.size() / 2 ?
                "-fx-text-fill: #34a853;" : "-fx-text-fill: #ea4335;");
        scoreLabel.setText("Score: " + score + "/" + questions.size());
        coinsLabel.setText("Pièces: " + coins + " 🪙");
    }

    @FXML
    private void handleFinish() {
        if (noteLabel.getScene() != null) {
            noteLabel.getScene().getWindow().hide();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

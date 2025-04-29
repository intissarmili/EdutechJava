package controllers.back;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import models.Question;
import service.CertificationService;
import service.QuestionService;
import service.QuizService;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class addQuestionController {

    @FXML private TextField questionField;
    @FXML private TextField option1Field;
    @FXML private TextField option2Field;
    @FXML private TextField option3Field;
    @FXML private ComboBox<String> correctAnswerCombo;
    @FXML private ComboBox<Integer> quizIdCombo;
    @FXML private ComboBox<Integer> certifIdCombo;

    private Consumer<Void> refreshCallback;
    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private final CertificationService certifService = new CertificationService();

    @FXML
    public void initialize() {
        // Configuration initiale
        correctAnswerCombo.getItems().addAll("Option 1", "Option 2", "Option 3");

        // Configuration des ComboBox
        quizIdCombo.setPromptText("Sélectionnez un quiz (optionnel)");
        certifIdCombo.setPromptText("Sélectionnez une certification (optionnel)");

        // Chargement asynchrone des données
        loadComboBoxData();
    }

    private void loadComboBoxData() {
        // Chargement des quiz dans un thread séparé
        new Thread(() -> {
            try {
                List<Integer> quizIds = quizService.readAll().stream()
                        .map(quiz -> quiz.getId())
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    quizIdCombo.getItems().clear();
                    quizIdCombo.getItems().add(null);
                    quizIdCombo.getItems().addAll(quizIds);
                });
            } catch (SQLException e) {
                Platform.runLater(() ->
                        showAlert("Erreur", "Échec du chargement des quiz: " + e.getMessage()));
            }
        }).start();

        // Chargement des certifications dans un thread séparé
        new Thread(() -> {
            try {
                List<Integer> certifIds = certifService.readAll().stream()
                        .map(certif -> certif.getId())
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    certifIdCombo.getItems().clear();
                    certifIdCombo.getItems().add(null);
                    certifIdCombo.getItems().addAll(certifIds);
                });
            } catch (SQLException e) {
                Platform.runLater(() ->
                        showAlert("Erreur", "Échec du chargement des certifications: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        try {
            if (!validateForm()) return;

            Question question = buildQuestionFromForm();
            questionService.create(question);

            handleSuccess();

        } catch (SQLException e) {
            handleDatabaseError(e);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Validation de la question
        String questionText = questionField.getText();
        if (questionText.length() < 10) {
            errors.append("- La question doit contenir au moins 10 caractères\n");
        }
        if (questionText.matches("^\\d.*")) {
            errors.append("- La question ne doit pas commencer par un chiffre\n");
        }

        // Validation des options
        if (option1Field.getText().isBlank() ||
                option2Field.getText().isBlank() ||
                option3Field.getText().isBlank()) {
            errors.append("- Toutes les options doivent être remplies\n");
        }

        // Validation réponse correcte
        if (correctAnswerCombo.getValue() == null) {
            errors.append("- Sélectionnez une réponse correcte\n");
        }

        // Validation au moins un ID sélectionné
        if (quizIdCombo.getValue() == null && certifIdCombo.getValue() == null) {
            errors.append("- Sélectionnez au moins un quiz ou une certification\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", errors.toString());
            return false;
        }
        return true;
    }

    private Question buildQuestionFromForm() {
        Question question = new Question();
        question.setQuestion(questionField.getText());
        question.setOptions(new String[]{
                option1Field.getText(),
                option2Field.getText(),
                option3Field.getText()
        });
        question.setReponseCorrecte(correctAnswerCombo.getValue());
        question.setQuizId(quizIdCombo.getValue());
        question.setCertificationId(certifIdCombo.getValue());
        return question;
    }

    private void handleSuccess() {
        // Rafraîchissement des données parentes
        if (refreshCallback != null) {
            refreshCallback.accept(null);
        }

        // Fermeture de la fenêtre
        closeWindow();
    }

    private void handleDatabaseError(SQLException e) {
        String message = e.getMessage().contains("foreign key") ?
                "L'ID sélectionné n'existe pas dans la base de données" :
                "Erreur base de données: " + e.getMessage();

        showAlert("Erreur BD", message);
    }

    private void closeWindow() {
        questionField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setRefreshCallback(Consumer<Void> callback) {
        this.refreshCallback = callback;
    }
}

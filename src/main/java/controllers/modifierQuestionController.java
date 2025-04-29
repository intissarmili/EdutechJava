package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Question;
import service.QuestionService;

import java.sql.SQLException;
import java.util.function.Consumer;

public class modifierQuestionController {

    @FXML private TextField questionField;
    @FXML private TextField option1Field;
    @FXML private TextField option2Field;
    @FXML private TextField option3Field;
    @FXML private ComboBox<String> correctAnswerCombo;
    @FXML private TextField quizIdField;
    @FXML private TextField certifIdField;

    private Question question;
    private Consumer<Void> refreshCallback;
    private final QuestionService questionService = new QuestionService();

    public void setQuestion(Question question) {
        this.question = question;
        if (question != null) {
            questionField.setText(question.getQuestion());
            String[] options = question.getOptions();
            option1Field.setText(options[0]);
            option2Field.setText(options[1]);
            option3Field.setText(options[2]);
            correctAnswerCombo.getItems().setAll(options);
            correctAnswerCombo.setValue(question.getReponseCorrecte());
            quizIdField.setText(String.valueOf(question.getQuizId()));
            certifIdField.setText(String.valueOf(question.getCertificationId()));
        }
    }

    public void setRefreshCallback(Consumer<Void> callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void handleSave() {
        try {
            question.setQuestion(questionField.getText());
            question.setOptions(new String[]{
                    option1Field.getText(),
                    option2Field.getText(),
                    option3Field.getText()
            });
            question.setReponseCorrecte(correctAnswerCombo.getValue());
            question.setQuizId(Integer.parseInt(quizIdField.getText()));
            question.setCertificationId(Integer.parseInt(certifIdField.getText()));

            questionService.update(question);

            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }
            closeWindow();
        } catch (SQLException | NumberFormatException e) {
            showAlert("Erreur", "Erreur: " + e.getMessage());
        }
    }


    // Dans ModifierQuestionController.java
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    private void closeWindow() {
        questionField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
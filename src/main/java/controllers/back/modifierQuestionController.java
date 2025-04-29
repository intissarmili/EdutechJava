package controllers.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Question;
import service.QuestionService;
import service.QuizService;
import service.CertificationService;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class modifierQuestionController {

    @FXML private TextField questionField;
    @FXML private TextField option1Field;
    @FXML private TextField option2Field;
    @FXML private TextField option3Field;
    @FXML private ComboBox<String> correctAnswerCombo;
    @FXML private ComboBox<Integer> quizIdCombo;
    @FXML private ComboBox<Integer> certifIdCombo;

    private Question question;
    private Consumer<Void> refreshCallback;
    private final QuestionService questionService = new QuestionService();
    private final QuizService quizService = new QuizService();
    private final CertificationService certifService = new CertificationService();

    @FXML
    public void initialize() {
        try {
            // Chargement initial des IDs
            loadComboBoxData();

            // Configuration des listeners
            setupOptionChangeListeners();

        } catch (SQLException e) {
            showAlert("Erreur Initialisation", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void loadComboBoxData() throws SQLException {
        // Chargement des IDs de quiz
        List<Integer> quizIds = quizService.getAllQuizIds();
        quizIdCombo.setItems(FXCollections.observableArrayList(quizIds));

        // Chargement des IDs de certification
        List<Integer> certifIds = certifService.getAllCertificationIds();
        certifIdCombo.setItems(FXCollections.observableArrayList(certifIds));
    }

    private void setupOptionChangeListeners() {
        // Mise à jour dynamique des options de réponse
        option1Field.textProperty().addListener((obs, oldVal, newVal) -> updateAnswerOptions());
        option2Field.textProperty().addListener((obs, oldVal, newVal) -> updateAnswerOptions());
        option3Field.textProperty().addListener((obs, oldVal, newVal) -> updateAnswerOptions());
    }

    private void updateAnswerOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
                option1Field.getText(),
                option2Field.getText(),
                option3Field.getText()
        );
        correctAnswerCombo.setItems(options);
    }

    public void setQuestion(Question question) {
        this.question = question;
        if (question != null) {
            // Initialisation des champs de base
            questionField.setText(question.getQuestion());

            // Initialisation des options
            String[] options = question.getOptions();
            if (options != null && options.length >= 3) {
                option1Field.setText(options[0]);
                option2Field.setText(options[1]);
                option3Field.setText(options[2]);
                correctAnswerCombo.setItems(FXCollections.observableArrayList(options));
                correctAnswerCombo.setValue(question.getReponseCorrecte());
            }

            // Sélection des IDs existants
            selectComboBoxValue(quizIdCombo, question.getQuizId());
            selectComboBoxValue(certifIdCombo, question.getCertificationId());
        }
    }

    private void selectComboBoxValue(ComboBox<Integer> comboBox, int value) {
        comboBox.getSelectionModel().clearSelection();
        ObservableList<Integer> items = comboBox.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == value) {
                comboBox.getSelectionModel().select(i);
                break;
            }
        }
    }

    public void setRefreshCallback(Consumer<Void> callback) {
        this.refreshCallback = callback;
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            updateQuestionFromFields();
            questionService.update(question);

            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }

            closeWindow();

        } catch (SQLException e) {
            showAlert("Erreur Sauvegarde", "Échec de la mise à jour: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (questionField.getText().isEmpty() ||
                option1Field.getText().isEmpty() ||
                option2Field.getText().isEmpty() ||
                option3Field.getText().isEmpty() ||
                correctAnswerCombo.getValue() == null ||
                quizIdCombo.getValue() == null ||
                certifIdCombo.getValue() == null) {

            showAlert("Validation", "Tous les champs doivent être remplis");
            return false;
        }
        return true;
    }

    private void updateQuestionFromFields() {
        question.setQuestion(questionField.getText());
        question.setOptions(new String[]{
                option1Field.getText(),
                option2Field.getText(),
                option3Field.getText()
        });
        question.setReponseCorrecte(correctAnswerCombo.getValue());
        question.setQuizId(quizIdCombo.getValue());
        question.setCertificationId(certifIdCombo.getValue());
    }

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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
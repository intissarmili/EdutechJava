package controllers.back;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Quiz;
import service.QuizService;

import java.sql.SQLException;

public class modifierQuizController {

    @FXML private TextField prixPieceField;
    @FXML private ComboBox<Integer> coursComboBox;
    @FXML private Button cancelBtn;
    @FXML private Button updateBtn;

    private QuizService quizService;
    private Quiz quiz;

    @FXML
    public void initialize() {
        quizService = new QuizService();
        setupButtons();

    }

    public void setQuizData(Quiz quiz) {
        this.quiz = quiz;
        prixPieceField.setText(String.valueOf(quiz.getPrixPiece()));
        coursComboBox.setValue(quiz.getCoursId());
    }

    private void setupButtons() {
        cancelBtn.setOnAction(event -> closeWindow());
        updateBtn.setOnAction(event -> handleUpdateQuiz());
    }

    private void handleUpdateQuiz() {
        if (!validateInputs()) {
            return;
        }

        try {
            quiz.setPrixPiece(Integer.parseInt(prixPieceField.getText()));
            quiz.setCoursId(coursComboBox.getValue());

            quizService.update(quiz);
            showAlert("Succès", "Modification réussie", "Le quiz a été modifié avec succès.", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format invalide", "Veuillez entrer une valeur numérique valide pour le prix pièce.", Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Validation du prix pièce
        try {
            int prixPiece = Integer.parseInt(prixPieceField.getText());
            if (prixPiece <= 0 || prixPiece > 700) {
                errors.append("- Le prix pièce doit être entre 1 et 700\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Le prix pièce doit être un nombre valide\n");
        }

        // Validation du cours
        if (coursComboBox.getValue() == null) {
            errors.append("- Vous devez sélectionner un cours\n");
        }

        if (errors.length() > 0) {
            showAlert("Erreur de validation", "Veuillez corriger les erreurs suivantes", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
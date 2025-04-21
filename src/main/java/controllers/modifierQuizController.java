package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Quiz;
import services.QuizService;
import services.CoursService;
import java.sql.SQLException;

public class modifierQuizController {

    @FXML private TextField prixPieceField;
    @FXML private ComboBox<Integer> coursComboBox;
    @FXML private Button cancelBtn;
    @FXML private Button updateBtn;

    private QuizService quizService;
    private CoursService coursService;
    private Quiz quiz;
    private Runnable onQuizUpdated;

    @FXML
    public void initialize() {
        quizService = new QuizService();
        coursService = new CoursService();
        setupButtons();
        populateCoursComboBox();
    }

    public void setOnQuizUpdated(Runnable callback) {
        this.onQuizUpdated = callback;
    }

    private void populateCoursComboBox() {
        try {
            coursComboBox.getItems().clear();
            coursComboBox.getItems().addAll(coursService.getAllCoursIds());

            coursComboBox.setCellFactory(lv -> new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("Cours #" + item);
                    }
                }
            });

            coursComboBox.setButtonCell(new ListCell<Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionnez un cours");
                    } else {
                        setText("Cours #" + item);
                    }
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger la liste des cours: " + e.getMessage());
        }
    }

    public void setQuizData(Quiz quiz) {
        this.quiz = quiz;
        prixPieceField.setText(String.valueOf(quiz.getPrixPiece()));

        if (quiz.getCoursId() != null) {
            coursComboBox.getSelectionModel().select(quiz.getCoursId());
        }
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
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Quiz modifié avec succès");

            if (onQuizUpdated != null) {
                onQuizUpdated.run();
            }

            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de modification",
                    "Échec de la modification du quiz: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide",
                    "Veuillez entrer une valeur numérique valide pour le prix pièce");
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        try {
            int prixPiece = Integer.parseInt(prixPieceField.getText());
            if (prixPiece <= 0 || prixPiece > 700) {
                errors.append("- Le prix pièce doit être entre 1 et 700\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Le prix pièce doit être un nombre valide\n");
        }

        if (coursComboBox.getValue() == null) {
            errors.append("- Vous devez sélectionner un cours\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreurs de validation", errors.toString());
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
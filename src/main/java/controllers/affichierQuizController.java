package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import models.Quiz;
import service.QuizService;
import java.io.IOException;
import java.sql.SQLException;

public class affichierQuizController {
    @FXML private TableView<Quiz> quizTable;
    @FXML private TableColumn<Quiz, Integer> idCol;
    @FXML private TableColumn<Quiz, Integer> noteCol;
    @FXML private TableColumn<Quiz, Integer> prixPieceCol;
    @FXML private TableColumn<Quiz, Integer> coursIdCol;
    @FXML private TableColumn<Quiz, String> questionsCol;
    @FXML private TableColumn<Quiz, Void> actionsCol;
    @FXML private ListView<String> questionsListView;
    @FXML private Button addBtn;
    @FXML private Button deleteBtn;
    @FXML private Button statsBtn; // Bouton pour les statistiques

    private final QuizService quizService = new QuizService();
    private final ObservableList<Quiz> quizzesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            configureTableColumns();
            setupActionsColumn();
            setupEventHandlers();

            refreshQuizData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Échec de l'initialisation du contrôleur: " + e.getMessage());
        }
    }

    private void configureTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        prixPieceCol.setCellValueFactory(new PropertyValueFactory<>("prixPiece"));
        coursIdCol.setCellValueFactory(new PropertyValueFactory<>("coursId"));
        questionsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuestionsConcatenated()));
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");

            {
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                editBtn.setOnAction(event -> {
                    Quiz quiz = getTableView().getItems().get(getIndex());
                    openEditWindow(quiz);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });
    }

    private void setupEventHandlers() {
        quizTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteBtn.setDisable(newVal == null);
            if (newVal != null) {
                questionsListView.getItems().setAll(newVal.getQuestionsConcatenated().split(", "));
            }
        });

        addBtn.setOnAction(event -> openAddWindow());
        deleteBtn.setOnAction(event -> deleteSelectedQuiz());
        statsBtn.setOnAction(event -> showStatistics()); // Gestionnaire pour le bouton stats
    }

    public void refreshQuizData() {
        try {
            quizzesList.setAll(quizService.readAll());
            quizTable.setItems(quizzesList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Échec du chargement des quizzes: " + e.getMessage());
        }
    }

    private void openAddWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuiz.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Quiz");
            stage.showAndWait();
            refreshQuizData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de fenêtre",
                    "Échec de l'ouverture de la fenêtre d'ajout: " + e.getMessage());
        }
    }

    private void openEditWindow(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierQuiz.fxml"));
            Parent root = loader.load();

            modifierQuizController controller = loader.getController();
            controller.setQuizData(quiz);
            controller.setOnQuizUpdated(this::refreshQuizData);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Quiz");
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de fenêtre",
                    "Échec de l'ouverture de la fenêtre de modification: " + e.getMessage());
        }
    }

    private void deleteSelectedQuiz() {
        Quiz selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer ce quiz?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    quizService.delete(selected);
                    quizzesList.remove(selected);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Quiz supprimé avec succès");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                            "Échec de la suppression du quiz: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuizStatistics.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques des Quizzes");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les statistiques: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
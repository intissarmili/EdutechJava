package controllers.back;

import controllers.modifierQuizController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
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

    private final QuizService quizService = new QuizService();
    private final ObservableList<Quiz> quizzesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            configureTableColumns();
            setupActionsColumn();
            setupEventHandlers();
            loadQuizData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Initialization Error",
                    "Failed to initialize controller: " + e.getMessage());
            e.printStackTrace();
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
    }

    private void loadQuizData() {
        quizzesList.setAll(quizService.readAll());
        quizTable.setItems(quizzesList);
    }

    private void openAddWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Back/AjouterQuiz.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Quiz");
            stage.showAndWait();
            loadQuizData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Window Error",
                    "Failed to open add window: " + e.getMessage());
        }
    }

    private void openEditWindow(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Back/ModifierQuiz.fxml"));
            Parent root = loader.load();

            modifierQuizController controller = loader.getController();
            controller.setQuizData(quiz);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Quiz");
            stage.showAndWait();
            loadQuizData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Window Error",
                    "Failed to open edit window: " + e.getMessage());
        }
    }

    private void deleteSelectedQuiz() {
        Quiz selected = quizTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer ce quiz?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                quizService.delete(selected);
                quizzesList.remove(selected);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Quiz supprimé avec succès");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
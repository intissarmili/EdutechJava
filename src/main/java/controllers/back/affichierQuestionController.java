package controllers.back;

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
import models.Question;
import service.QuestionService;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class affichierQuestionController {

    @FXML private TableView<Question> tableView;
    @FXML private TableColumn<Question, Integer> idCol;
    @FXML private TableColumn<Question, Integer> quizIdCol;
    @FXML private TableColumn<Question, Integer> certifIdCol;
    @FXML private TableColumn<Question, String> questionCol;
    @FXML private TableColumn<Question, String> optionsCol;
    @FXML private TableColumn<Question, Void> actionsColum;

    @FXML private Button btnAddQuestion;
    @FXML private Button btnSupQuestion;

    private final QuestionService questionService = new QuestionService();
    private final ObservableList<Question> questionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadQuestions();
        setupSelectionListener();
    }

    private void configureTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        quizIdCol.setCellValueFactory(new PropertyValueFactory<>("quizId"));
        certifIdCol.setCellValueFactory(new PropertyValueFactory<>("certificationId"));
        questionCol.setCellValueFactory(new PropertyValueFactory<>("question"));
        optionsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(" | ", cellData.getValue().getOptions()))
        );

        setupActionsColumn();
    }





    @FXML
    private void btnAddQuestion(ActionEvent event) {
        try {
            // Chemin relatif depuis le package du contrôleur
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/back/AjouterQuestion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupActionsColumn() {
        actionsColum.setCellFactory(param -> new TableCell<>() {
            private final Button modifyButton = new Button("✏️ Modifier");

            {
                modifyButton.setStyle("-fx-background-color: #1cacd2; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 12px; "
                        + "-fx-background-radius: 3;");

                modifyButton.setOnAction(event -> {
                    Question question = getTableView().getItems().get(getIndex());
                    openModificationWindow(question);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyButton);
            }
        });
    }

    private void loadQuestions() {
        try {
            questionList.setAll(questionService.readAll());
            tableView.setItems(questionList);
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSelectionListener() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) ->
                        btnSupQuestion.setDisable(newSelection == null)
        );
    }

    @FXML
    private void handleAddQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Back/AjouterQuestion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une Question");
            stage.showAndWait();

            loadQuestions(); // Rafraîchir après ajout
        } catch (IOException e) {
            showAlert("Erreur", "Ouverture impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openModificationWindow(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Back/ModifierQuestion.fxml"));
            Parent root = loader.load();

            modifierQuestionController controller = loader.getController();
            controller.setQuestion(question);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Question");
            stage.showAndWait();

            loadQuestions();
        } catch (IOException e) {
            showAlert("Erreur", "Modification impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteQuestion() {
        Question selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner une question", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Suppression définitive");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette question ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                questionService.delete(selected);
                questionList.remove(selected);
                showAlert("Succès", "Suppression effectuée", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de suppression : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
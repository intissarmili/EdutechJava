package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Cours;
import service.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class affichierCoursController {
    @FXML private TableView<Cours> TableCours;
    @FXML private TableColumn<Cours, Integer> IdcoursColum;
    @FXML private TableColumn<Cours, Integer> IdcertifColum;
    @FXML private TableColumn<Cours, String> idTitreColum;
    @FXML private TableColumn<Cours, String> idContenuColum;
    @FXML private TableColumn<Cours, String> idCategorieColum;
    @FXML private TableColumn<Cours, Void> actionsColum;

    private final CoursService coursService = new CoursService();
    private final ObservableList<Cours> coursList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadCoursData();
    }

    private void configureTableColumns() {
        IdcoursColum.setCellValueFactory(new PropertyValueFactory<>("id"));
        IdcertifColum.setCellValueFactory(new PropertyValueFactory<>("certificationId"));
        idTitreColum.setCellValueFactory(new PropertyValueFactory<>("titre"));
        idContenuColum.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        idCategorieColum.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        actionsColum.setCellFactory(param -> new TableCell<>() {
            private final Button modifyButton = new Button("✏️ Modifier");

            {
                modifyButton.setStyle("-fx-background-color: #1cacd2; "
                        + "-fx-text-fill: white; "
                        + "-fx-font-weight: bold; "
                        + "-fx-padding: 3 6; "
                        + "-fx-font-size: 12px; "
                        + "-fx-background-radius: 3;");

                modifyButton.setOnAction(event -> {
                    Cours cours = getTableView().getItems().get(getIndex());
                    openModificationWindow(cours);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyButton);
            }
        });
    }

    private void loadCoursData() {
        try {
            coursList.setAll(coursService.readAll());
            TableCours.setItems(coursList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cours: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openModificationWindow(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCours.fxml"));
            Parent root = loader.load();

            ModifierCoursController controller = loader.getController();
            controller.setCoursToEdit(cours);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Cours");
            stage.showAndWait();

            loadCoursData();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification", Alert.AlertType.ERROR);
        }
    }

    // Méthodes de navigation standardisées
    @FXML
    public void naviguerVersBlogCours(ActionEvent event) {
        naviguerVers("/BlogCours.fxml", event);
    }

    @FXML
    public void naviguerVersAffichierCours(ActionEvent event) {
        naviguerVers("/affichierCours.fxml", event);
    }

    @FXML
    public void naviguerVersAffichierCertificat(ActionEvent event) {
        naviguerVers("/affichierCertification.fxml", event);
    }

    @FXML
    public void naviguerVersAffichierQuiz(ActionEvent event) {
        naviguerVers("/affichierQuiz.fxml", event);
    }

    @FXML
    public void naviguerVersAffichierQuestion(ActionEvent event) {
        naviguerVers("/affichierQuestion.fxml", event);
    }

    @FXML
    public void naviguerVersAjoutCours(ActionEvent event) {
        naviguerVers("/AjouterCours.fxml", event);
    }

    private void naviguerVers(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            TableCours.getScene().setRoot(root);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            showAlert("Erreur", "Impossible de charger la page: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void supprimerCours(ActionEvent event) {
        Cours selectedCours = TableCours.getSelectionModel().getSelectedItem();
        if (selectedCours == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un cours à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Suppression du cours");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce cours définitivement ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                coursService.delete(selectedCours);
                coursList.remove(selectedCours);
                showAlert("Succès", "Le cours a été supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur lors de la suppression: " + ex.getMessage(), Alert.AlertType.ERROR);
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
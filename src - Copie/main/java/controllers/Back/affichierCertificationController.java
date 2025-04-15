package controllers.Back;

import controllers.modifierCertificationController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Certification;
import services.CertificationService;

import java.io.IOException;
import java.sql.SQLException;

public class affichierCertificationController {

    @FXML private TableView<Certification> certificationTable;
    @FXML private TableColumn<Certification, Integer> idCol;
    @FXML private TableColumn<Certification, String> nomCol;
    @FXML private TableColumn<Certification, String> descriptionCol;
    @FXML private TableColumn<Certification, Double> prixCol;
    @FXML private TableColumn<Certification, Integer> prixPieceCol;
    @FXML private TableColumn<Certification, Integer> noteCol;
    @FXML private TableColumn<Certification, Void> actionsCol;
    @FXML private Button addBtn;
    @FXML private Button deleteBtn;

    private CertificationService certificationService;
    private ObservableList<Certification> certificationsList;

    @FXML
    public void initialize() {
        certificationService = new CertificationService();
        setupTable();
        setupButtons();
        loadCertifications();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        prixPieceCol.setCellValueFactory(new PropertyValueFactory<>("prixPiece"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Modifier");
            {
                editBtn.setStyle("-fx-background-color: #1cacd2; "
                        + "-fx-text-fill: white; "
                        + "-fx-font-weight: bold; "
                        + "-fx-padding: 3 6; "
                        + "-fx-font-size: 12px; "
                        + "-fx-background-radius: 3;");

                editBtn.setOnAction(event -> {
                    Certification certification = getTableView().getItems().get(getIndex());
                    handleEditCertification(certification);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });

        certificationTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    deleteBtn.setDisable(newSelection == null);
                });
    }

    private void setupButtons() {
        addBtn.setOnAction(event -> handleAddCertification());
        deleteBtn.setOnAction(event -> handleDeleteSelectedCertification());

        addBtn.setStyle("-fx-background-color: #16ac63; "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 8 20; "
                + "-fx-font-size: 14px; "
                + "-fx-background-radius: 3;");

        deleteBtn.setStyle("-fx-background-color: #e74c3c; "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 8 20; "
                + "-fx-font-size: 14px; "
                + "-fx-background-radius: 3;");
    }

    private void loadCertifications() {
        try {
            certificationsList = FXCollections.observableArrayList(certificationService.readAll());
            certificationTable.setItems(certificationsList);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddCertification() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Back/AjouterCertification.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Certification");
            stage.showAndWait();
            loadCertifications(); // Rafraîchir la liste après ajout
        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteSelectedCertification() {
        Certification selectedCertification = certificationTable.getSelectionModel().getSelectedItem();
        if (selectedCertification != null) {
            handleDeleteCertification(selectedCertification);
        } else {
            showAlert("Avertissement", "Aucune sélection", "Veuillez sélectionner une certification à supprimer.", Alert.AlertType.WARNING);
        }
    }

    private void handleEditCertification(Certification certification) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Back/ModifierCertification.fxml"));
            Parent root = loader.load();

            modifierCertificationController controller = loader.getController();
            controller.setCertificationData(certification);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Certification");
            stage.showAndWait();
            loadCertifications(); // Rafraîchir la liste après modification
        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteCertification(Certification certification) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la certification");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + certification.getNom() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    certificationService.delete(certification);
                    certificationsList.remove(certification);
                    showAlert("Succès", "Suppression réussie", "La certification a été supprimée.", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de suppression", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
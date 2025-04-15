package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.CategoryEvent;
import service.CategoryEventService;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ListeCategoryEventController {

    @FXML
    private TableView<CategoryEvent> tableView;

    @FXML
    private TableColumn<CategoryEvent, String> colLocation;

    @FXML
    private TableColumn<CategoryEvent, String> colType;

    @FXML
    private TableColumn<CategoryEvent, String> colDuration;

    @FXML
    private TableColumn<CategoryEvent, Void> colActions;

    private CategoryEventService service = new CategoryEventService();
    private ObservableList<CategoryEvent> categoryList = FXCollections.observableArrayList();

    public void initialize() {
        setupTableColumns();
        refreshTable();
    }

    private void setupTableColumns() {
        // Configuration des colonnes
        colLocation.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        colDuration.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuration()));

        // Colonne d'actions (Modifier / Supprimer)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-base: #4CAF50;");
                btnDelete.setStyle("-fx-base: #f44336;");

                btnEdit.setOnAction(event -> {
                    CategoryEvent category = getTableView().getItems().get(getIndex());
                    openEditForm(category);
                });

                btnDelete.setOnAction(event -> {
                    CategoryEvent category = getTableView().getItems().get(getIndex());
                    confirmAndDelete(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    public void refreshTable() {
        List<CategoryEvent> categories = service.getAll();
        categoryList.clear();
        categoryList.addAll(categories);
        tableView.setItems(categoryList);
    }

    @FXML
    public void openAddForm(ActionEvent event) {
        try {
            // Essayez plusieurs chemins possibles
            URL resourceUrl = null;
            String[] possiblePaths = {
                    "/resources/vue/AjouterCategoryEvent.fxml",
                    "/vue/AjouterCategoryEvent.fxml",
                    "../vue/AjouterCategoryEvent.fxml",
                    "/view/AjouterCategoryEvent.fxml",
                    "AjouterCategoryEvent.fxml"
            };

            for (String path : possiblePaths) {
                resourceUrl = getClass().getResource(path);
                if (resourceUrl != null) {
                    System.out.println("Fichier trouvé à : " + path);
                    break;
                }
            }

            if (resourceUrl == null) {
                throw new IOException("Impossible de trouver le fichier FXML 'AjouterCategoryEvent.fxml'. Vérifiez qu'il existe et qu'il est accessible.");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter une catégorie d'événement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchir la table après fermeture
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace(); // Pour voir l'erreur complète dans la console
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openEditForm(CategoryEvent category) {
        try {
            // Essayez plusieurs chemins possibles
            URL resourceUrl = null;
            String[] possiblePaths = {
                    "/resources/vue/EditCategoryEvent.fxml",
                    "/vue/EditCategoryEvent.fxml",
                    "../vue/EditCategoryEvent.fxml",
                    "/view/EditCategoryEvent.fxml",
                    "EditCategoryEvent.fxml"
            };

            for (String path : possiblePaths) {
                resourceUrl = getClass().getResource(path);
                if (resourceUrl != null) {
                    System.out.println("Fichier trouvé à : " + path);
                    break;
                }
            }

            if (resourceUrl == null) {
                throw new IOException("Impossible de trouver le fichier FXML 'EditCategoryEvent.fxml'. Vérifiez qu'il existe et qu'il est accessible.");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            EditCategoryEventController controller = loader.getController();
            controller.initData(category);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier une catégorie d'événement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchir la table après fermeture
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace(); // Pour voir l'erreur complète dans la console
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmAndDelete(CategoryEvent category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie d'événement ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.delete(category);
                refreshTable();
                showAlert("Succès", "Catégorie supprimée avec succès", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
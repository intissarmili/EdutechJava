package controller.back;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.util.Optional;

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

    @FXML
    private TextField searchField;

    private CategoryEventService service = new CategoryEventService();
    private ObservableList<CategoryEvent> categoryList = FXCollections.observableArrayList();
    private FilteredList<CategoryEvent> filteredData;

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur...");
        setupTableColumns();
        refreshTable();

        if (searchField != null) {
            setupSearchFilter();
        } else {
            System.out.println("searchField est null");
        }
    }

    private void setupTableColumns() {
        System.out.println("Configuration des colonnes...");
        colLocation.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        colDuration.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuration()));

        // Colonne d'actions avec gestion des erreurs
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-base: #4CAF50;");
                btnDelete.setStyle("-fx-base: #f44336;");

                btnEdit.setOnAction(event -> {
                    try {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            CategoryEvent category = getTableView().getItems().get(index);
                            System.out.println("Édition de la catégorie: " + category.getType());
                            openEditForm(category);
                        } else {
                            System.out.println("Index hors limites: " + index);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'édition: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("Erreur", "Une erreur s'est produite lors de l'édition: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                });

                btnDelete.setOnAction(event -> {
                    try {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            CategoryEvent category = getTableView().getItems().get(index);
                            System.out.println("Suppression de la catégorie: " + category.getType());
                            confirmAndDelete(category);
                        } else {
                            System.out.println("Index hors limites: " + index);
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("Erreur", "Une erreur s'est produite lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupSearchFilter() {
        System.out.println("Configuration du filtre de recherche...");
        filteredData = new FilteredList<>(categoryList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(category -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (category.getLocation().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (category.getType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (category.getDuration().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<CategoryEvent> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML
    public void refreshTable() {
        System.out.println("Rafraîchissement de la table...");
        try {
            List<CategoryEvent> categories = service.getAll();
            System.out.println("Nombre de catégories récupérées: " + categories.size());
            categoryList.clear();
            categoryList.addAll(categories);

            if (filteredData != null) {
                // Le prédicat sera réappliqué automatiquement
            } else {
                tableView.setItems(categoryList);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement de la table: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors du chargement des données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void openAddForm(ActionEvent event) {
        System.out.println("Ouverture du formulaire d'ajout...");
        try {
            // Chercher le fichier FXML
            FXMLLoader loader = new FXMLLoader();
            URL resourceUrl = getClass().getResource("/vue/AjouterCategoryEvent.fxml");

            if (resourceUrl == null) {
                // Essayer d'autres chemins si le premier échoue
                resourceUrl = getClass().getResource("../vue/AjouterCategoryEvent.fxml");
            }

            if (resourceUrl == null) {
                // Essayer un chemin relatif
                resourceUrl = getClass().getResource("AjouterCategoryEvent.fxml");
            }

            if (resourceUrl == null) {
                // Dernière tentative, chemin plus général
                resourceUrl = getClass().getClassLoader().getResource("vue/AjouterCategoryEvent.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Impossible de trouver le fichier FXML 'AjouterCategoryEvent.fxml'");
            }

            System.out.println("Ressource FXML trouvée à: " + resourceUrl);
            loader.setLocation(resourceUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter une catégorie d'événement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchir la table après fermeture
            refreshTable();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openEditForm(CategoryEvent category) {
        System.out.println("Ouverture du formulaire d'édition...");
        try {
            // Chercher le fichier FXML
            FXMLLoader loader = new FXMLLoader();
            URL resourceUrl = getClass().getResource("/vue/EditCategoryEvent.fxml");

            if (resourceUrl == null) {
                // Essayer d'autres chemins si le premier échoue
                resourceUrl = getClass().getResource("../vue/EditCategoryEvent.fxml");
            }

            if (resourceUrl == null) {
                // Essayer un chemin relatif
                resourceUrl = getClass().getResource("EditCategoryEvent.fxml");
            }

            if (resourceUrl == null) {
                // Dernière tentative, chemin plus général
                resourceUrl = getClass().getClassLoader().getResource("vue/EditCategoryEvent.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Impossible de trouver le fichier FXML 'EditCategoryEvent.fxml'");
            }

            System.out.println("Ressource FXML trouvée à: " + resourceUrl);
            loader.setLocation(resourceUrl);
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
            System.err.println("Erreur lors de l'ouverture du formulaire d'édition: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmAndDelete(CategoryEvent category) {
        System.out.println("Confirmation de suppression...");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie d'événement ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(category);
                System.out.println("Catégorie supprimée avec succès");
                refreshTable();
                showAlert("Succès", "Catégorie supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression: " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur", "Une erreur s'est produite lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
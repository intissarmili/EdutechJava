package controllers;

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
import models.CategoryEvent;
import service.CategoryEventService;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

public class ListeCategoryEventController {

    @FXML private TableView<CategoryEvent> tableView;
    @FXML private TableColumn<CategoryEvent, String> colLocation;
    @FXML private TableColumn<CategoryEvent, String> colType;
    @FXML private TableColumn<CategoryEvent, String> colDuration;
    @FXML private TableColumn<CategoryEvent, Void> colActions;
    @FXML private Label totalCategoriesLabel;
    @FXML private Pagination tablePagination;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;

    private final CategoryEventService service = new CategoryEventService();
    private final ObservableList<CategoryEvent> categoryList = FXCollections.observableArrayList();
    private final FilteredList<CategoryEvent> filteredData = new FilteredList<>(categoryList, p -> true);

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        setupTypeFilter();
        refreshTable();
    }

    private void setupTableColumns() {
        colLocation.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocation()));
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));
        colDuration.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDuration()));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                // Style des boutons
                btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 5 10; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 5 10; -fx-cursor: hand;");

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

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(createPredicate(newValue));
            updateTotalCount();
        });

        // Lier les données filtrées à la table
        SortedList<CategoryEvent> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setupTypeFilter() {
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(createPredicate(searchField.getText()));
            updateTotalCount();
        });
    }

    private Predicate<CategoryEvent> createPredicate(String searchText) {
        return category -> {
            if (searchText == null || searchText.isEmpty()) {
                return applyTypeFilter(category);
            }

            String lowerCaseFilter = searchText.toLowerCase();
            return (category.getLocation().toLowerCase().contains(lowerCaseFilter) ||
                    category.getType().toLowerCase().contains(lowerCaseFilter)) &&
                    applyTypeFilter(category);
        };
    }

    private boolean applyTypeFilter(CategoryEvent category) {
        String selectedType = filterComboBox.getSelectionModel().getSelectedItem();
        return selectedType == null || selectedType.isEmpty() || selectedType.equals("Tous les types") ||
                category.getType().equalsIgnoreCase(selectedType);
    }

    @FXML
    public void refreshTable() {
        try {
            List<CategoryEvent> categories = service.getAll();
            categoryList.setAll(categories);

            // Mettre à jour le filtre des types
            updateTypeFilter(categories);

            updateTotalCount();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des catégories", Alert.AlertType.ERROR);
        }
    }

    private void updateTypeFilter(List<CategoryEvent> categories) {
        ObservableList<String> types = FXCollections.observableArrayList("Tous les types");
        categories.stream()
                .map(CategoryEvent::getType)
                .distinct()
                .sorted()
                .forEach(types::add);

        filterComboBox.setItems(types);
    }

    private void updateTotalCount() {
        totalCategoriesLabel.setText(String.valueOf(filteredData.size()));
    }

    @FXML
    private void openAddForm(ActionEvent event) {
        loadFormWindow("/vue/AjouterCategoryEvent.fxml", "Ajouter une catégorie");
    }

    private void openEditForm(CategoryEvent category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/EditCategoryEvent.fxml"));
            Parent root = loader.load();

            controllers.EditCategoryEventController controller = loader.getController();
            controller.initData(category);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier une catégorie");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
        }
    }

    private void loadFormWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", Alert.AlertType.ERROR);
        }
    }

    private void confirmAndDelete(CategoryEvent category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie: " + category.getType());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie ? Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.delete(category);
                    refreshTable();
                    showAlert("Succès", "Catégorie supprimée avec succès", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
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
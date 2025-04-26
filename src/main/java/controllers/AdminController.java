package controllers.user;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import services.user.IUserService;
import services.user.UserService;

import java.io.IOException;

public class AdminController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TextField searchField;

    private final IUserService userService = new UserService();
    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        firstNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        roleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRoles()));
        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhoneNumber()));

        loadUsers();

        // 🔍 Make searchField dynamic
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterUsersByName(newText);
        });
    }

    private void loadUsers() {
        userList = FXCollections.observableArrayList(userService.getAllUsers());
        userTable.setItems(userList);
    }

    private void filterUsersByName(String query) {
        if (query == null || query.isEmpty()) {
            userTable.setItems(userList);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : userList) {
            String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
            if (fullName.contains(lowerCaseQuery)) {
                filteredList.add(user);
            }
        }

        userTable.setItems(filteredList);
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/AjouterBack.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadUsers();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger l'interface d'ajout !");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/ModifierBack.fxml"));
                Parent root = loader.load();

                ModifierBackController controller = loader.getController();
                controller.setUser(selected);

                Stage stage = new Stage();
                stage.setTitle("Modifier l'utilisateur");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                loadUsers();
            } catch (IOException e) {
                showError("Erreur", "Impossible de charger l'interface de modification !");
                e.printStackTrace();
            }
        } else {
            showError("Erreur", "Veuillez sélectionner un utilisateur !");
        }
    }

    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userService.deleteUser(selected.getId());
            showInfo("Succès", "Utilisateur supprimé !");
            loadUsers();
        } else {
            showError("Erreur", "Veuillez sélectionner un utilisateur !");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }
}

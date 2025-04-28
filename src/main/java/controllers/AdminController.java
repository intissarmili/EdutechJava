package controllers.user;

import javafx.application.Platform;
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
import utils.Session;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AdminController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> bannedColumn;
    @FXML private TableColumn<User, String> approvedColumn; // ✅ New approved status column
    @FXML private TextField searchField;

    private final IUserService userService = new UserService();
    private ObservableList<User> userList;
    private Timer banCheckTimer;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        firstNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        roleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRoles()));
        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhoneNumber()));
        bannedColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isBanned() ? "Banni" : "Actif"
        ));
        approvedColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isApproved() ? "Validé" : "En attente"
        )); // ✅ Display approved status

        loadUsers();
        startBanMonitor();

        searchField.textProperty().addListener((obs, oldText, newText) -> filterUsersByName(newText));
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
    private void handleAdd() { openWindow("/views/user/AjouterBack.fxml", "Ajouter un utilisateur"); }

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

    @FXML
    private void handleBan() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userService.banUser(selected.getId());
            showInfo("Succès", "Utilisateur banni !");
            loadUsers();
        } else {
            showError("Erreur", "Veuillez sélectionner un utilisateur !");
        }
    }

    @FXML
    private void handleUnban() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userService.unbanUser(selected.getId());
            showInfo("Succès", "Utilisateur débanni !");
            loadUsers();
        } else {
            showError("Erreur", "Veuillez sélectionner un utilisateur !");
        }
    }

    @FXML
    private void handleApprove() { // ✅ Approve pending user
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isApproved()) {
            selected.setApproved(true);
            userService.updateUser(selected);
            showInfo("Succès", "Utilisateur validé !");
            loadUsers();
        } else {
            showError("Erreur", "Aucun utilisateur sélectionné ou déjà validé !");
        }
    }

    @FXML
    private void handleOpenLogs() {
        openWindow("/views/user/AdminLog.fxml", "Historique des Connexions");
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la fenêtre !");
        }
    }


    private void startBanMonitor() {
        banCheckTimer = new Timer();
        banCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                User currentUser = Session.getCurrentUser();
                if (currentUser != null) {
                    IUserService userService = new UserService();
                    User freshUser = userService.getUserByEmail(currentUser.getEmail());
                    if (freshUser.isBanned()) {
                        Platform.runLater(() -> {
                            forceLogout("Votre compte admin a été banni !");
                        });
                        banCheckTimer.cancel();
                    }
                }
            }
        }, 0, 5000);
    }

    private void forceLogout(String message) {
        try {
            Session.setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EduTech - Connexion");
            stage.show();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Déconnecté");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleOpenDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/AdminDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EduTech - Dashboard Admin");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

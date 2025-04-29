package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.User;
import service.user.UserService;

import java.util.List;

public class UserListController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colFirstName;

    @FXML
    private TableColumn<User, String> colLastName;

    @FXML
    private TableColumn<User, String> colRoles;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colRoles.setCellValueFactory(new PropertyValueFactory<>("roles"));

        // Charger les données
        loadUsers();
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers();
        ObservableList<User> observableList = FXCollections.observableArrayList(users);
        userTable.setItems(observableList);
    }
}

package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import service.user.UserService;

public class AjouterBackController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private ComboBox<String> roleComboBox;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT");
    }

    @FXML
    void handleAddUser() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneNumberField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || role == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        User newUser = new User(email, password, firstName, lastName, phone, role);
        userService.addUser(newUser);

        showAlert("Succès", "Utilisateur ajouté avec succès !");
        ((Stage) emailField.getScene().getWindow()).close(); // Ferme la fenêtre
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }
}

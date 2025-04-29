package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import service.user.UserService;

public class ModifierBackController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private ComboBox<String> roleComboBox;

    private final UserService userService = new UserService();
    private User selectedUser;

    public void setUser(User user) {
        this.selectedUser = user;

        if (user != null) {
            emailField.setText(user.getEmail());
            passwordField.setText(user.getPassword());
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            phoneNumberField.setText(user.getPhoneNumber());
            roleComboBox.setValue(user.getRoles());
        }
    }

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT");
    }

    @FXML
    void handleUpdateUser() {
        if (selectedUser == null) {
            showAlert("Erreur", "Aucun utilisateur sélectionné.");
            return;
        }

        // Validation de base (optionnel)
        if (emailField.getText().isEmpty() || passwordField.getText().isEmpty() || roleComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires !");
            return;
        }

        selectedUser.setEmail(emailField.getText().trim());
        selectedUser.setPassword(passwordField.getText().trim());
        selectedUser.setFirstName(firstNameField.getText().trim());
        selectedUser.setLastName(lastNameField.getText().trim());
        selectedUser.setPhoneNumber(phoneNumberField.getText().trim());
        selectedUser.setRoles(roleComboBox.getValue());

        userService.updateUser(selectedUser);
        showAlert("Succès", "Utilisateur modifié !");
        ((Stage) emailField.getScene().getWindow()).close(); // Ferme la fenêtre
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

package controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import models.User;
import services.user.IUserService;
import services.user.UserService;
import utils.Session;

public class UpdateUserController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneNumberField;

    private IUserService userService = new UserService();

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user != null) {
            emailField.setText(user.getEmail());
            passwordField.setText(user.getPassword());
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            phoneNumberField.setText(user.getPhoneNumber());
        }
    }

    @FXML
    void handleUpdate(MouseEvent event) {
        User user = Session.getCurrentUser();
        if (user == null) {
            showAlert("Erreur", "Aucun utilisateur connecté.");
            return;
        }

        user.setEmail(emailField.getText().trim());
        user.setPassword(passwordField.getText().trim());
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setPhoneNumber(phoneNumberField.getText().trim());

        userService.updateUser(user);
        showAlert("Succès", "Utilisateur mis à jour avec succès !");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}

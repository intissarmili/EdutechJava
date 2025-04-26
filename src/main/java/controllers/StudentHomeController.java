package controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import models.User;
import utils.Session;

public class StudentHomeController {

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();

        if (user != null) {
            fullNameLabel.setText("Nom complet : " + user.getFirstName() + " " + user.getLastName());
            emailLabel.setText("Email : " + user.getEmail());
            phoneLabel.setText("Téléphone : " + user.getPhoneNumber());
            roleLabel.setText("Rôle : " + user.getRoles());
        } else {
            showAlert("Erreur", "Aucun utilisateur connecté.");
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Session.setCurrentUser(null);
        ((Stage) fullNameLabel.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

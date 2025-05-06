package controllers.user;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import models.User;
import services.user.IUserService;
import services.user.UserService;
import utils.Session;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class StudentHomeController {

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;

    private Timer banCheckTimer;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();

        if (user != null) {
            fullNameLabel.setText("Nom complet : " + user.getFirstName() + " " + user.getLastName());
            emailLabel.setText("Email : " + user.getEmail());
            phoneLabel.setText("Téléphone : " + user.getPhoneNumber());
            roleLabel.setText("Rôle : " + user.getRoles());
            startBanMonitor();
        } else {
            showAlert("Erreur", "Aucun utilisateur connecté.");
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
                            forceLogout("Votre compte a été banni !");
                        });
                        banCheckTimer.cancel();
                    }
                }
            }
        }, 0, 5000); // Check every 5 seconds
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Session.setCurrentUser(null);
        if (banCheckTimer != null) {
            banCheckTimer.cancel();
        }
        ((Stage) fullNameLabel.getScene().getWindow()).close();
    }

    private void forceLogout(String message) {
        try {
            Session.setCurrentUser(null);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

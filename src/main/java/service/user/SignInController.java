package controllers.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import models.User;
import services.user.IUserService;
import services.user.UserService;
import utils.Session;

import java.io.IOException;

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final IUserService userService = new UserService();

    @FXML
    void handleLogin(MouseEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (userService.login(email, password)) {
            User user = userService.getUserByEmail(email);
            Session.setCurrentUser(user); // stocke l’utilisateur connecté

            showAlert("Succès", "Connexion réussie !");
            String role = user.getRoles();
            String fxml = null;

            switch (role) {
                case "ROLE_ADMIN":
                    fxml = "/views/user/Admin.fxml";
                    break;
                case "ROLE_STUDENT":
                    fxml = "/views/user/StudentHome.fxml"; // ✅ on redirige vers le profil complet
                    break;
                case "ROLE_TEACHER":
                    fxml = "/views/user/TeacherHome.fxml";
                    break;

                default:
                    showAlert("Erreur", "Rôle inconnu !");
                    return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("EduTech - " + role.replace("ROLE_", ""));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec du chargement de l'interface !");
            }

        } else {
            showAlert("Erreur", "Email ou mot de passe incorrect.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}

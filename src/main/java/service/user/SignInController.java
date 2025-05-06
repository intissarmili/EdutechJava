package controllers.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import models.User;
import services.user.IUserService;
import services.user.UserService;
import services.user.UserLogService;
import utils.Session;
import javafx.event.ActionEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private WebView captchaWebView;

    private final IUserService userService = new UserService();
    private final UserLogService userLogService = new UserLogService();

    private final String SITE_KEY = "6LdMoSYrAAAAAOr5F3wci4ejIm9KFMB3n7hy6mgw";
    private final String SECRET_KEY = "6LdMoSYrAAAAAEl1FVNaw3QVe_sCnFlxR4hTrlMA";

    private WebEngine webEngine;

    @FXML
    public void initialize() {
        webEngine = captchaWebView.getEngine();
        loadCaptcha();
    }

    private void loadCaptcha() {
        String html = "<html><head><script src='https://www.google.com/recaptcha/api.js'></script></head>"
                + "<body><form action=''><div class='g-recaptcha' data-sitekey='" + SITE_KEY + "'></div></form></body></html>";
        webEngine.loadContent(html);
    }

    @FXML
    void handleLogin(MouseEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (userService.login(email, password)) {
            connectUser(email);
        } else {
            User user = userService.getUserByEmail(email);
            if (user != null && user.isBanned()) {
                showAlert("Accès refusé", "Votre compte est banni.");
            } else {
                showAlert("Erreur", "Email ou mot de passe incorrect.");
            }
        }
    }

    @FXML
    void handleFingerprintLogin(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer votre email pour identifier votre compte !");
            return;
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            showAlert("Erreur", "Utilisateur non trouvé !");
            return;
        }

        if (!user.isUseFingerprintLogin()) {
            showAlert("Erreur", "Ce compte n'a pas activé l'authentification par empreinte digitale !");
            return;
        }

        // Simulate fingerprint success (later you will integrate real SDK here)
        boolean fingerprintSuccess = simulateFingerprintScan();

        if (fingerprintSuccess) {
            connectUser(email);
        } else {
            showAlert("Erreur", "Échec de la reconnaissance d'empreinte !");
        }
    }

    private boolean simulateFingerprintScan() {
        // ➡️ TODO: integrate real SDK fingerprint scan here later!
        return true; // For now: always succeed (for test)
    }

    private void connectUser(String email) {
        User user = userService.getUserByEmail(email);

        if (!user.isApproved()) {
            showAlert("Validation en attente", "Votre compte est en attente de validation par un administrateur.");
            return;
        }

        Session.setCurrentUser(user);
        userLogService.addLog(user.getId(), "login");

        showAlert("Succès", "Connexion réussie !");
        String role = user.getRoles();
        String fxml = null;

        switch (role) {
            case "ROLE_ADMIN":
                fxml = "/views/user/Admin.fxml";
                break;
            case "ROLE_STUDENT":
                fxml = "/views/user/StudentHome.fxml";
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
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    private void handleForgetPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/ForgetPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Erreur chargement ForgetPassword.fxml : " + e.getMessage());
        }
    }
    @FXML
    private void handleBackToSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un compte");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page d'inscription !");
        }
    }


}

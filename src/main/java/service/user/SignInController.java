package service.user;

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
import service.user.IUserService;
import service.user.UserService;
import service.user.UserLogService;
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

    private final String SITE_KEY = "6LdMoSYrAAAAAOr5F3wci4ejIm9KFMB3n7hy6mgw"; // your real Site Key
    private final String SECRET_KEY = "6LdMoSYrAAAAAEl1FVNaw3QVe_sCnFlxR4hTrlMA"; // your real Secret Key

    private WebEngine webEngine;

    @FXML
    public void initialize() {
        webEngine = captchaWebView.getEngine();
        loadCaptcha();
    }

    private void loadCaptcha() {
        String html = "<html><head><script src='https://www.google.com/recaptcha/api.js'></script></head>"
                + "<body>"
                + "<form action=''>"
                + "<div class='g-recaptcha' data-sitekey='" + SITE_KEY + "'></div>"
                + "</form>"
                + "</body></html>";
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

        // If you want to reactivate captcha verification later:
        // String token = (String) webEngine.executeScript("document.querySelector('textarea[name=\"g-recaptcha-response\"]').value");
        // if (token == null || token.isEmpty()) {
        //     showAlert("Erreur", "Veuillez valider le CAPTCHA !");
        //     return;
        // }
        // if (!verifyCaptcha(token)) {
        //     showAlert("Erreur", "Échec de la vérification CAPTCHA !");
        //     return;
        // }

        if (userService.login(email, password)) {
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

        } else {
            User user = userService.getUserByEmail(email);
            if (user != null && user.isBanned()) {
                showAlert("Accès refusé", "Votre compte est banni. Veuillez contacter l'administration.");
            } else {
                showAlert("Erreur", "Email ou mot de passe incorrect.");
            }
        }
    }

    private boolean verifyCaptcha(String token) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify";
            String params = "secret=" + SECRET_KEY + "&response=" + token;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                output.writeBytes(params);
                output.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString().contains("\"success\": true");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
}

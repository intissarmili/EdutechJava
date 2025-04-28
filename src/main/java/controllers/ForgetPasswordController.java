package controllers.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import models.User;
import services.user.UserService;
import utils.EmailUtil;

import java.util.Random;

public class ForgetPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button sendCodeButton;
    @FXML private Button verifyButton;
    @FXML private Button resetPasswordButton;

    private final UserService userService = new UserService();
    private String generatedCode;

    @FXML
    public void initialize() {
        verifyButton.setDisable(true);
        resetPasswordButton.setDisable(true);
        codeField.setDisable(true);
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
    }

    @FXML
    private void handleSendCode(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir votre email !");
            return;
        }

        if (!userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email non trouvé !");
            return;
        }

        // Générer un code aléatoire
        generatedCode = generateRandomCode();

        boolean emailSent = EmailUtil.sendEmail(email, "Code de réinitialisation", "Votre code est : " + generatedCode);

        if (emailSent) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Code envoyé à votre email !");
            codeField.setDisable(false);
            verifyButton.setDisable(false);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'envoi de l'email !");
        }
    }

    @FXML
    private void handleVerifyCode(ActionEvent event) {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.equals(generatedCode)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Code vérifié !");
            newPasswordField.setDisable(false);
            confirmPasswordField.setDisable(false);
            resetPasswordButton.setDisable(false);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Code incorrect !");
        }
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir les deux champs !");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas !");
            return;
        }

        if (newPassword.length() < 5) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe doit contenir au moins 5 caractères !");
            return;
        }

        User user = userService.getUserByEmail(email);
        if (user != null) {
            user.setPassword(newPassword);
            userService.updateUser(user);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe mis à jour !");
            redirectToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur introuvable !");
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // génère 6 chiffres
        return String.valueOf(code);
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/SignIn.fxml")); // ✅ correct page
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion"); // optional: set window title
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur redirection vers SignIn: " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

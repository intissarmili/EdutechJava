package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import service.user.IUserService;
import service.user.UserService;
import utils.EmailUtil;

import java.util.Random;

public class SignUpController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField verificationCodeField;

    private final IUserService userService = new UserService();
    private String generatedCode;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN");
        roleComboBox.setValue(null);
    }

    @FXML
    void handleSendVerificationCode(ActionEvent event) {
        String email = emailField.getText().trim();
        if (!email.matches("^[\\w.-]+@(gmail\\.com|yahoo\\.fr)$")) {
            showAlert("Erreur", "Entrez une adresse email valide (gmail/yahoo) !");
            return;
        }

        generatedCode = String.format("%06d", new Random().nextInt(999999));
        boolean success = EmailUtil.sendEmail(email, "Code de vérification", "Voici votre code : " + generatedCode);

        if (success) {
            showAlert("Info", "Code envoyé à votre adresse mail !");
        } else {
            showAlert("Erreur", "Échec d'envoi de l'email.");
        }
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneNumberField.getText().trim();
        String role = roleComboBox.getValue();
        String inputCode = verificationCodeField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || role == null || inputCode.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        if (!inputCode.equals(generatedCode)) {
            showAlert("Erreur", "Code de vérification incorrect !");
            return;
        }

        if (!firstName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
            showAlert("Erreur", "Le prénom et le nom ne doivent contenir que des lettres !");
            return;
        }

        if (!email.matches("^[\\w.-]+@(gmail\\.com|yahoo\\.fr)$")) {
            showAlert("Erreur", "Email invalide (ex : @gmail.com ou @yahoo.fr) !");
            return;
        }

        if (!phone.matches("^\\d{8}$")) {
            showAlert("Erreur", "Le numéro de téléphone doit contenir exactement 8 chiffres !");
            return;
        }

        if (password.length() < 5) {
            showAlert("Erreur", "Le mot de passe doit contenir au moins 5 caractères !");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert("Erreur", "Cette adresse email est déjà utilisée !");
            return;
        }

        User newUser = new User(email, password, role, firstName, lastName, phone);
        newUser.setEmailVerified(true);
        newUser.setBanned(false);

        if (role.equals("ROLE_ADMIN")) {
            newUser.setApproved(false); // 🛑 Admin needs manual approval!
        } else {
            newUser.setApproved(true); // ✅ Students and Teachers are auto-approved
        }

        userService.addUser(newUser);

        if (role.equals("ROLE_ADMIN")) {
            showAlert("Succès", "Votre demande d'inscription Admin est envoyée. En attente de validation par un administrateur.");
        } else {
            showAlert("Succès", "Utilisateur créé avec succès !");
        }

        clearFields();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> redirectToSignIn(event));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        phoneNumberField.clear();
        roleComboBox.setValue(null);
        verificationCodeField.clear();
    }

    private void redirectToSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion - EduTech");
            stage.show();
            ((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
            System.err.println("Erreur redirection vers SignIn: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        redirectToSignIn(event);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

package controllers.user;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.User;
import utils.Session;

public class WelcomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label roleLabel;

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getFirstName() + " " + currentUser.getLastName());
            roleLabel.setText("Rôle : " + currentUser.getRoles());
        } else {
            welcomeLabel.setText("Bienvenue !");
            roleLabel.setText("");
        }
    }
}

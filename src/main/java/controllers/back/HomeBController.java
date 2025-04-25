package controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class HomeBController {

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentPane;
    @FXML private Button btnListeFeed;

    @FXML
    public void initialize() {
        setupNavigation();
    }

    private void setupNavigation() {
        btnListeFeed.setOnAction(e -> loadContent("/Back/feed_commentaire.fxml"));

    }

    private void loadContent(String fxmlPath) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de " + fxmlPath);
            e.printStackTrace();
        }
    }
}
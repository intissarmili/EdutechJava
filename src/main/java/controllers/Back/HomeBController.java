package controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class HomeBController {

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentPane;
    @FXML private Button btnListeCours, btnListeCertificats, btnListeQuestions, btnListeQuiz;

    @FXML
    public void initialize() {
        setupNavigation();
    }

    private void setupNavigation() {
        btnListeCours.setOnAction(e -> loadContent("/Back/AffichierCours.fxml"));
        btnListeCertificats.setOnAction(e -> loadContent("/Back/AffichierCertificationB.fxml"));
        btnListeQuestions.setOnAction(e -> loadContent("/Back/AffichierQuestion.fxml"));
        btnListeQuiz.setOnAction(e -> loadContent("/Back/AffichierQuiz.fxml"));
    }

    private void loadContent(String fxmlPath) {
        try {
            Node content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de " + fxmlPath);
            e.printStackTrace();
        }
    }
}
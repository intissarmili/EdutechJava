package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class Home extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogCours.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1024, 768);
            primaryStage.setTitle("Gestion des Quiz");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("ERREUR: Fichier FXML introuvable");
            System.err.println("Vérifiez que AffichierQuiz.fxml est dans src/main/resources/");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
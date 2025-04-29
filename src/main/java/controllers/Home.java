package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Home extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogCours.fxml"));
            Parent root = loader.load();

            // Set up the stage
            primaryStage.setTitle("Tutor Availability System");
            primaryStage.setScene(new Scene(root, 1024, 768));

            // Prevent window from being resized too small
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // You might want to show an alert to the user here
            System.err.println("Failed to load the FXML file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
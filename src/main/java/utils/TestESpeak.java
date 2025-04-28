package utils;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Simple test application for eSpeak TTS
 * Run this class to test if eSpeak is working
 */
public class TestESpeak extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Check if eSpeak is available and display status
        boolean available = ESpeakTTS.isAvailable();
        Label statusLabel = new Label("eSpeak " + (available ? "is available!" : "NOT found!"));
        statusLabel.setStyle(available ? "-fx-text-fill: green; -fx-font-weight: bold;" : 
                                       "-fx-text-fill: red; -fx-font-weight: bold;");
        
        // Path information
        Label pathLabel = new Label("Path information is in console output");
        
        // Text input field
        TextField textField = new TextField("Ceci est un test de synthèse vocale");
        
        // Test button
        Button testButton = new Button("Test Speech");
        testButton.setOnAction(e -> {
            String text = textField.getText();
            boolean result = ESpeakTTS.speak(text);
            System.out.println("Speech result: " + result);
        });
        
        // Layout
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20px;");
        root.getChildren().addAll(statusLabel, pathLabel, textField, testButton);
        
        // Scene setup
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("eSpeak Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 
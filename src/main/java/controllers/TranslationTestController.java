package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import service.TranslationService;

public class TranslationTestController {

    @FXML
    private TextArea sourceTextArea;
    
    @FXML
    private TextArea translatedTextArea;
    
    @FXML
    private ComboBox<String> targetLanguageComboBox;
    
    @FXML
    private Button translateButton;
    
    private final TranslationService translationService = new TranslationService();
    
    @FXML
    public void initialize() {
        // Populate language dropdown
        targetLanguageComboBox.getItems().addAll(translationService.getAvailableLanguages());
        targetLanguageComboBox.setValue(TranslationService.ENGLISH); // Default to English
        
        // Set up the translate button
        translateButton.setOnAction(event -> translateText());
    }
    
    @FXML
    private void translateText() {
        String sourceText = sourceTextArea.getText();
        String targetLanguage = targetLanguageComboBox.getValue();
        
        if (sourceText == null || sourceText.trim().isEmpty()) {
            translatedTextArea.setText("Please enter text to translate");
            return;
        }
        
        try {
            String translatedText = translationService.translate(sourceText, targetLanguage);
            translatedTextArea.setText(translatedText);
        } catch (Exception e) {
            translatedTextArea.setText("Translation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ListFeed.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) sourceTextArea.getScene().getWindow();
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
} 
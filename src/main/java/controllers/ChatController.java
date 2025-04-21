package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import services.HuggingFaceService;

public class ChatController {

    @FXML private TextField messageField;
    @FXML private TextArea responseArea;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingSpinner;

    private final HuggingFaceService hfService = new HuggingFaceService("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");

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        errorLabel.setText("");
        responseArea.setText("");
        loadingSpinner.setVisible(true);

        if (message.isEmpty()) {
            loadingSpinner.setVisible(false);
            errorLabel.setText("Erreur : Le message ne peut pas être vide.");
            return;
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return hfService.generateResponse(message);
            }
        };

        task.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            responseArea.setText("Réponse :\n" + task.getValue());
        });

        task.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            errorLabel.setText("Erreur : " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

}

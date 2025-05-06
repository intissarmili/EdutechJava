package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import service.HuggingFaceService;

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

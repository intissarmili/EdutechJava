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

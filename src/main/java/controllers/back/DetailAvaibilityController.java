package controllers.back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import models.avaibility;
import service.AvaibilityService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class DetailAvaibilityController implements Initializable {

    @FXML
    private Label dateLabel;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label endTimeLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Label tutorLabel;

    @FXML
    private Label statusLabel;

    private avaibility currentAvaibility;
    private AvaibilityService avaibilityService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        avaibilityService = new AvaibilityService();
    }

    public void setAvaibility(avaibility avaibility) {
        this.currentAvaibility = avaibility;
        populateFields();
    }

    public void loadAvaibility(int id) {
        try {
            currentAvaibility = avaibilityService.getById(id);
            if(currentAvaibility != null) {
                populateFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Disponibilité non trouvée.");
                backAction(null);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la disponibilité: " + e.getMessage());
        }
    }

    private void populateFields() {
        if(currentAvaibility == null) return;

        // Format date
        dateLabel.setText(currentAvaibility.getDate());

        // Format start time
        startTimeLabel.setText(currentAvaibility.getStartTime());

        // Format end time
        endTimeLabel.setText(currentAvaibility.getEndTime());

        // Calculate duration
        try {
            LocalTime start = LocalTime.parse(currentAvaibility.getStartTime());
            LocalTime end = LocalTime.parse(currentAvaibility.getEndTime());
            Duration duration = Duration.between(start, end);
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            if (hours > 0) {
                durationLabel.setText(hours + "h" + (minutes > 0 ? " " + minutes + "min" : ""));
            } else {
                durationLabel.setText(minutes + " minutes");
            }
        } catch (Exception e) {
            durationLabel.setText("Non disponible");
        }

        // Set tutor information - replace with actual tutor name retrieval
        tutorLabel.setText("Tuteur ID: " + currentAvaibility.getTutorId());

        // Set status - this would be based on your business logic
        statusLabel.setText("Disponible");
    }

    @FXML
    void backAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/listCards.fxml"));
            Parent root = loader.load();
            dateLabel.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation: " + e.getMessage());
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        try {
            avaibilityService.delete(currentAvaibility.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Disponibilité supprimée avec succès.");
            backAction(null);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

   @FXML
    void handleUpdate(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/update.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the availability
            ModifierAvaibilityController controller = loader.getController();
            controller.setAvaibilityToUpdate(currentAvaibility);

            dateLabel.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
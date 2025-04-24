package controllers.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.avaibility;
import service.AvaibilityService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class AjouterAvaibilityController implements Initializable {

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> startHourComboBox;

    @FXML
    private ComboBox<String> startMinuteComboBox;

    @FXML
    private ComboBox<String> endHourComboBox;

    @FXML
    private ComboBox<String> endMinuteComboBox;

    @FXML
    private Label dateErrorLabel;

    @FXML
    private Label startTimeErrorLabel;

    @FXML
    private Label endTimeErrorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> hours = FXCollections.observableArrayList();
        ObservableList<String> minutes = FXCollections.observableArrayList();

        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }

        for (int i = 0; i < 60; i += 5) {
            minutes.add(String.format("%02d", i));
        }

        startHourComboBox.setItems(hours);
        startMinuteComboBox.setItems(minutes);
        endHourComboBox.setItems(hours);
        endMinuteComboBox.setItems(minutes);

        // Valeurs par défaut
        datePicker.setValue(LocalDate.now());
        startHourComboBox.setValue("09");
        startMinuteComboBox.setValue("00");
        endHourComboBox.setValue("10");
        endMinuteComboBox.setValue("00");

        // Restrict date selection to only today and next month
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate oneMonthLater = today.plusMonths(1);

                setDisable(empty || date.compareTo(today) < 0 || date.compareTo(oneMonthLater) > 0);

                if (date.compareTo(today) < 0 || date.compareTo(oneMonthLater) > 0) {
                    setStyle("-fx-background-color: #ffc0cb;"); // Light red for invalid dates
                }
            }
        });

        dateErrorLabel.setVisible(false);
        startTimeErrorLabel.setVisible(false);
        endTimeErrorLabel.setVisible(false);

        // Add listeners to validate end time > start time when selection changes
        startHourComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validateTimes());
        startMinuteComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validateTimes());
        endHourComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validateTimes());
        endMinuteComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> validateTimes());
    }
    @FXML private AnchorPane createFormRoot;
    @FXML private StackPane contentPane;




    @FXML
    private void handleCancelOrBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/listCards.fxml"));
            Parent root = loader.load();

            // Get current scene dimensions
            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            double width = currentStage.getWidth();
            double height = currentStage.getHeight();

            // Create new scene with the same dimensions
            Scene scene = new Scene(root, width, height);

            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void validateTimes() {
        String sh = startHourComboBox.getValue();
        String sm = startMinuteComboBox.getValue();
        String eh = endHourComboBox.getValue();
        String em = endMinuteComboBox.getValue();

        // Only validate if all values are selected
        if (sh != null && sm != null && eh != null && em != null) {
            try {
                LocalTime start = LocalTime.of(Integer.parseInt(sh), Integer.parseInt(sm));
                LocalTime end = LocalTime.of(Integer.parseInt(eh), Integer.parseInt(em));

                if (!start.isBefore(end)) {
                    endTimeErrorLabel.setText("L'heure de fin doit être après l'heure de début.");
                    endTimeErrorLabel.setVisible(true);
                } else {
                    endTimeErrorLabel.setVisible(false);
                }
            } catch (NumberFormatException e) {
                // Handle parsing errors
            }
        }
    }

    @FXML
    void saveavaibilityAction(ActionEvent event) {
        dateErrorLabel.setVisible(false);
        startTimeErrorLabel.setVisible(false);
        endTimeErrorLabel.setVisible(false);

        boolean hasError = false;

        try {
            // Vérification date
            LocalDate date = datePicker.getValue();
            LocalDate today = LocalDate.now();
            LocalDate oneMonthLater = today.plusMonths(1);

            if (date == null) {
                dateErrorLabel.setText("Veuillez sélectionner une date.");
                dateErrorLabel.setVisible(true);
                hasError = true;
            } else if (date.isBefore(today)) {
                dateErrorLabel.setText("La date ne peut pas être dans le passé.");
                dateErrorLabel.setVisible(true);
                hasError = true;
            } else if (date.isAfter(oneMonthLater)) {
                dateErrorLabel.setText("La date ne peut pas être plus d'un mois dans le futur.");
                dateErrorLabel.setVisible(true);
                hasError = true;
            }

            // Vérification heures
            String sh = startHourComboBox.getValue();
            String sm = startMinuteComboBox.getValue();
            String eh = endHourComboBox.getValue();
            String em = endMinuteComboBox.getValue();

            if (sh == null || sm == null) {
                startTimeErrorLabel.setText("Veuillez sélectionner une heure de début.");
                startTimeErrorLabel.setVisible(true);
                hasError = true;
            }

            if (eh == null || em == null) {
                endTimeErrorLabel.setText("Veuillez sélectionner une heure de fin.");
                endTimeErrorLabel.setVisible(true);
                hasError = true;
            }

            if (hasError) return;

            LocalTime start = LocalTime.of(Integer.parseInt(sh), Integer.parseInt(sm));
            LocalTime end = LocalTime.of(Integer.parseInt(eh), Integer.parseInt(em));

            if (!start.isBefore(end)) {
                endTimeErrorLabel.setText("L'heure de fin doit être après l'heure de début.");
                endTimeErrorLabel.setVisible(true);
                return;
            }

            // Conversion
            Date startTime = Date.from(date.atTime(start).atZone(ZoneId.systemDefault()).toInstant());
            Date endTime = Date.from(date.atTime(end).atZone(ZoneId.systemDefault()).toInstant());

            int tutorId = 1; // temporaire, à remplacer par l'utilisateur connecté

            avaibility availability = new avaibility(date.toString(), start.toString(), end.toString(), tutorId);
            AvaibilityService service = new AvaibilityService();
            service.add(availability);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Disponibilité ajoutée avec succès !");
            // Redirection
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/details.fxml"));
            Parent root = loader.load();


            // Get the controller and pass the availability
            DetailAvaibilityController detailController = loader.getController();

// Either pass the availability object directly
            detailController.setAvaibility(availability);
// OR pass the ID (if you have it after adding to database)
// detailController.loadAvaibility(availability.getId());

            datePicker.getScene().setRoot(root);
















            datePicker.getScene().setRoot(root);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/avaibility/list.fxml"));
            Parent root = loader.load();
            datePicker.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
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
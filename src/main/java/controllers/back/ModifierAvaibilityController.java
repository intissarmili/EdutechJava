package controllers.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.avaibility;
import service.AvaibilityService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class ModifierAvaibilityController implements Initializable {

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

    private avaibility currentAvaibility;
    private AvaibilityService avaibilityService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        avaibilityService = new AvaibilityService();

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

    public void setAvaibilityToUpdate(avaibility avaibility) {
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
                cancelAction(null);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la disponibilité: " + e.getMessage());
        }
    }

    private void populateFields() {
        if(currentAvaibility == null) return;

        // Set the date
        try {
            LocalDate date = LocalDate.parse(currentAvaibility.getDate());
            datePicker.setValue(date);
        } catch (Exception e) {
            // Handle date parsing error
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Format de date invalide. Utilisation de la date actuelle.");
            datePicker.setValue(LocalDate.now());
        }

        // Set the start time
        try {
            LocalTime startTime = LocalTime.parse(currentAvaibility.getStartTime());
            startHourComboBox.setValue(String.format("%02d", startTime.getHour()));
            startMinuteComboBox.setValue(String.format("%02d", startTime.getMinute()));
        } catch (Exception e) {
            // Handle time parsing error
            startHourComboBox.setValue("09");
            startMinuteComboBox.setValue("00");
        }

        // Set the end time
        try {
            LocalTime endTime = LocalTime.parse(currentAvaibility.getEndTime());
            endHourComboBox.setValue(String.format("%02d", endTime.getHour()));
            endMinuteComboBox.setValue(String.format("%02d", endTime.getMinute()));
        } catch (Exception e) {
            // Handle time parsing error
            endHourComboBox.setValue("10");
            endMinuteComboBox.setValue("00");
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
    void updateAvaibilityAction(ActionEvent event) {
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

            // Update the current availability object
            currentAvaibility.setDate(date.toString());
            currentAvaibility.setStartTime(start.toString());
            currentAvaibility.setEndTime(end.toString());

            // Update in database
            avaibilityService.update(currentAvaibility);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Disponibilité mise à jour avec succès !");

            // Redirection to details page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/details.fxml"));
            Parent root = loader.load();

            // Pass updated availability to details controller
            DetailAvaibilityController detailController = loader.getController();
            detailController.setAvaibility(currentAvaibility);

            datePicker.getScene().setRoot(root);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        try {
            if (currentAvaibility != null) {
                // Return to details page if we have an availability
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/details.fxml"));
                Parent root = loader.load();

                DetailAvaibilityController detailController = loader.getController();
                detailController.setAvaibility(currentAvaibility);

                datePicker.getScene().setRoot(root);
            } else {
                // Return to list page if no availability
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/avaibility/list.fxml"));
                Parent root = loader.load();
                datePicker.getScene().setRoot(root);
            }
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
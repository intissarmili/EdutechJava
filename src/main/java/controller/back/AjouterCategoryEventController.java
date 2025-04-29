package controller.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.CategoryEvent;
import service.CategoryEventService;

import java.net.URL;
import java.util.ResourceBundle;

public class AjouterCategoryEventController implements Initializable {

    @FXML
    private ComboBox<String> cbLocation;

    @FXML
    private TextField tfType;

    @FXML
    private TextField tfDuration;

    @FXML
    private Button addButton;

    private final CategoryEventService service = new CategoryEventService();

    // Liste des gouvernorats tunisiens
    private final ObservableList<String> gouvernorats = FXCollections.observableArrayList(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte",
            "Béja", "Jendouba", "Le Kef", "Siliana", "Sousse", "Monastir", "Mahdia",
            "Sfax", "Kairouan", "Kasserine", "Sidi Bouzid", "Gabès", "Médenine",
            "Tataouine", "Gafsa", "Tozeur", "Kébili"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du contrôleur AjouterCategoryEventController");

        // Vérifier que la ComboBox est correctement injectée
        if (cbLocation == null) {
            System.err.println("ERREUR: ComboBox cbLocation est null!");
            return;
        }

        try {
            // Initialisation de la ComboBox avec les gouvernorats
            cbLocation.setItems(gouvernorats);

            // Sélectionner le premier élément par défaut
            cbLocation.getSelectionModel().selectFirst();

            System.out.println("ComboBox initialisée avec " + gouvernorats.size() + " gouvernorats");
            System.out.println("Valeur sélectionnée: " + cbLocation.getValue());

            // Configuration des validateurs
            setupValidators();
            validateAllFields();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupValidators() {
        // Validation pour la ComboBox
        cbLocation.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Valeur de la ComboBox changée: " + newVal);
            validateAllFields();
        });

        // Validation pour le champ Type
        tfType.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(tfType, newVal.length() >= 6);
            validateAllFields();
        });

        // Validation pour la durée
        tfDuration.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(tfDuration, isValidDuration(newVal));
            validateAllFields();
        });
    }

    private boolean isValidDuration(String duration) {
        try {
            String[] parts = duration.split(":");

            // Vérifier le format HH:MM
            if (parts.length != 2) {
                return false;
            }

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            // Vérifier que les minutes sont entre 0-59
            if (minutes < 0 || minutes > 59) {
                return false;
            }

            // Vérifier que les heures sont positives
            if (hours < 0) {
                return false;
            }

            int total = hours * 60 + minutes;
            return total >= 60; // Au moins 1 heure
        } catch (Exception e) {
            return false;
        }
    }

    private void validateField(TextField field, boolean valid) {
        field.setStyle(valid ?
                "-fx-border-color: #2ecc71; -fx-border-width: 2px;" :
                "-fx-border-color: #e74c3c; -fx-border-width: 2px;");
    }

    private void validateAllFields() {
        boolean locationValid = cbLocation.getValue() != null && !cbLocation.getValue().isEmpty();
        boolean typeValid = tfType.getText() != null && tfType.getText().length() >= 6;
        boolean durationValid = tfDuration.getText() != null && isValidDuration(tfDuration.getText());

        boolean allValid = locationValid && typeValid && durationValid;

        addButton.setDisable(!allValid);
    }

    @FXML
    private void handleAdd() {
        try {
            System.out.println("Ajout d'une catégorie d'événement:");
            System.out.println("- Location: " + cbLocation.getValue());
            System.out.println("- Type: " + tfType.getText());
            System.out.println("- Duration: " + tfDuration.getText());

            CategoryEvent event = new CategoryEvent(
                    cbLocation.getValue(),
                    tfType.getText(),
                    tfDuration.getText()
            );

            boolean success = service.add(event);

            if(success) {
                showAlert("Succès", "Catégorie d'événement ajoutée avec succès!", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Erreur", "Échec de l'ajout de la catégorie", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("Exception lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) cbLocation.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
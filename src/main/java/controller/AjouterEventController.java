package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CategoryEvent;
import model.Event;
import service.CategoryEventService;
import service.Eventservice;
import service.WeatherService;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class AjouterEventController implements Initializable {
    @FXML private DatePicker datePicker;
    @FXML private Label weatherLabel;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    private static final String API_KEY = "votre_clé_api";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast";
    @FXML
    private ComboBox<CategoryEvent> categoryField;
     @FXML
    private TextField timeField;
    @FXML
    private Button photoButton;
    @FXML
    private Label photoPathLabel;
    @FXML
    private Label validationMessageLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Eventservice eventService;
    private CategoryEventService categoryService;
    private String photoPath = null;
    private final String UPLOAD_DIR = "uploads/";
    private List<Event> existingEvents;
    @FXML
    private Button applyFilterButton;
    private WeatherService weatherService;
    private static final String DEFAULT_CITY = "Tunis";
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eventService = new Eventservice();
        categoryService = new CategoryEventService();
        weatherService = new WeatherService();
        // Réinitialiser le message de validation
        validationMessageLabel.setText("");

        // Charger tous les événements pour vérifier les titres dupliqués
        existingEvents = eventService.getAll();

        // Charger les catégories disponibles
        loadCategories();

        // Configuration du DatePicker pour n'accepter que les dates futures
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                // Désactiver les dates passées
                setDisable(empty || date.compareTo(today) < 0);

                // Style visuel pour montrer les dates désactivées
                if (date.compareTo(today) < 0) {
                    setStyle("-fx-background-color: #ffc0cb;"); // Rose pâle pour dates passées
                }
            }
        });

        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDescription(newValue);
        });

        // Configurer l'affichage des catégories dans le ComboBox
        categoryField.setCellFactory(lv -> new ListCell<CategoryEvent>() {
            @Override
            protected void updateItem(CategoryEvent category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.getType()); // Afficher le type de catégorie
                }
            }
        });

        categoryField.setButtonCell(new ListCell<CategoryEvent>() {
            @Override
            protected void updateItem(CategoryEvent category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.getType()); // Afficher le type de catégorie
                }
            }
        });

        // Ajout d'écouteurs pour la validation en temps réel
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTitle(newValue);
        });

        timeField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTime(newValue);
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDate(newValue);
        });

        // Désactiver le bouton de sauvegarde initialement jusqu'à ce que tous les champs soient valides
        saveButton.setDisable(true);
    }

    private void loadCategories() {
        try {
            List<CategoryEvent> categories = categoryService.getAll();
            ObservableList<CategoryEvent> categoryList = FXCollections.observableArrayList(categories);
            categoryField.setItems(categoryList);

            if (!categoryList.isEmpty()) {
                categoryField.setValue(categoryList.get(0));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des catégories",
                    "Impossible de charger les catégories d'événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("Le titre est obligatoire");
            return;
        }

        // Vérification de la longueur minimale (6 caractères)
        if (title.trim().length() < 6) {
            titleField.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            validationMessageLabel.setText("Le titre doit contenir au moins 6 caractères");
            return;
        }

        // Vérifier si le titre existe déjà
        boolean isDuplicate = existingEvents.stream()
                .anyMatch(e -> e.getTitle().equalsIgnoreCase(title.trim()));

        if (isDuplicate) {
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("Ce titre existe déjà. Veuillez choisir un titre unique.");
        } else {
            titleField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            validationMessageLabel.setText("");
        }

        validateAllFields();
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            descriptionArea.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("La description est obligatoire");
            return;
        }

        // Vérification de la longueur minimale (10 caractères)
        if (description.trim().length() < 10) {
            descriptionArea.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            validationMessageLabel.setText("La description doit contenir au moins 10 caractères");
            return;
        }

        descriptionArea.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        validationMessageLabel.setText("");

        validateAllFields();
    }

    private void validateTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.trim().isEmpty()) {
                timeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                validationMessageLabel.setText("L'heure est obligatoire");
                return;
            }

            // Essayer de parser l'heure (format HH:mm)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime.parse(timeStr.trim(), formatter);
            timeField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            validationMessageLabel.setText("");
        } catch (DateTimeParseException e) {
            timeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
        }

        validateAllFields();
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            datePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("La date est obligatoire");
            return;
        }

        // Vérifier si la date est dans le futur
        LocalDate today = LocalDate.now();
        if (date.isBefore(today) || date.isEqual(today)) {
            datePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("La date doit être dans le futur");
        } else {
            datePicker.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            validationMessageLabel.setText("");
        }

        validateAllFields();
    }

    private void validateAllFields() {
        boolean titleValid = titleField.getText() != null &&
                !titleField.getText().trim().isEmpty() &&
                titleField.getText().trim().length() >= 6 &&
                titleField.getStyle().contains("green");

        boolean descriptionValid = descriptionArea.getText() != null &&
                !descriptionArea.getText().trim().isEmpty() &&
                descriptionArea.getText().trim().length() >= 10 &&
                descriptionArea.getStyle().contains("green");

        boolean categoryValid = categoryField.getValue() != null;
        boolean dateValid = datePicker.getValue() != null &&
                datePicker.getValue().isAfter(LocalDate.now()) &&
                datePicker.getStyle().contains("green");

        boolean timeValid = false;
        try {
            if (timeField.getText() != null && !timeField.getText().trim().isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime.parse(timeField.getText().trim(), formatter);
                timeValid = timeField.getStyle().contains("green");
            }
        } catch (Exception e) {
            timeValid = false;
        }

        // Activer/désactiver le bouton de sauvegarde
        saveButton.setDisable(!(titleValid && descriptionValid && categoryValid && dateValid && timeValid));
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) titleField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Créer le répertoire d'upload s'il n'existe pas
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Générer un nom de fichier unique
                String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destination = Paths.get(UPLOAD_DIR + uniqueFileName);

                // Copier le fichier dans le répertoire d'upload
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Stocker le chemin relatif pour la base de données
                photoPath = destination.toString();
                photoPathLabel.setText(selectedFile.getName());

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du téléchargement de l'image",
                        e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            CategoryEvent selectedCategory = categoryField.getValue();
            int categoryId = selectedCategory.getId();

            LocalDate date = datePicker.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime time = LocalTime.parse(timeField.getText().trim(), formatter);
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            Event event = new Event(title, description, categoryId, dateTime, photoPath);
            eventService.add(event);

            // Ajouter l'événement à la liste locale pour éviter les doublons
            existingEvents.add(event);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté",
                    "L'événement a été ajouté avec succès.");

            clearForm();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'événement",
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errors.append("Le titre est obligatoire.\n");
        } else {
            // Vérifier l'unicité du titre
            String title = titleField.getText().trim();
            boolean isDuplicate = existingEvents.stream()
                    .anyMatch(e -> e.getTitle().equalsIgnoreCase(title));

            if (isDuplicate) {
                errors.append("Ce titre existe déjà. Veuillez choisir un titre unique.\n");
            }
        }

        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            errors.append("La description est obligatoire.\n");
        }

        if (categoryField.getValue() == null) {
            errors.append("Veuillez sélectionner une catégorie.\n");
        }

        if (datePicker.getValue() == null) {
            errors.append("Veuillez sélectionner une date.\n");
        } else {
            // Vérifier que la date est future
            LocalDate selectedDate = datePicker.getValue();
            if (!selectedDate.isAfter(LocalDate.now())) {
                errors.append("La date doit être dans le futur.\n");
            }
        }

        try {
            if (timeField.getText() == null || timeField.getText().trim().isEmpty()) {
                errors.append("L'heure est obligatoire.\n");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime.parse(timeField.getText().trim(), formatter);
            }
        } catch (DateTimeParseException e) {
            errors.append("Format d'heure invalide. Utilisez le format HH:MM.\n");
        }

        if (errors.length() > 0) {
            validationMessageLabel.setText(errors.toString());
            return false;
        }

        return true;
    }

    private void clearForm() {
        titleField.clear();
        titleField.setStyle("");
        descriptionArea.clear();
        descriptionArea.setStyle("");
        datePicker.setValue(null);
        datePicker.setStyle("");
        timeField.clear();
        timeField.setStyle("");
        photoPath = null;
        photoPathLabel.setText("Aucune image sélectionnée");
        validationMessageLabel.setText("");

        // Désactiver le bouton de sauvegarde après effacement du formulaire
        saveButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
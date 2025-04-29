package controller.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CategoryEvent;
import model.Event;
import model.WeatherData;
import service.CategoryEventService;
import service.Eventservice;
import service.WeatherService;
import service.DescriptionGeneratorService;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;
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
import javafx.application.Platform;

public class AjouterEventController implements Initializable {
    @FXML
    private Label locationLabel;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<CategoryEvent> categoryField;
    @FXML
    private DatePicker datePicker;
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
    @FXML
    private Label weatherLabel;
    @FXML
    private ImageView weatherIconView;

    private Eventservice eventService;
    private CategoryEventService categoryService;
    private WeatherService weatherService;
    private DescriptionGeneratorService descriptionGenerator;
    private String photoPath = null;
    private final String UPLOAD_DIR = "uploads/";
    private List<Event> existingEvents;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eventService = new Eventservice();
        categoryService = new CategoryEventService();
        weatherService = new WeatherService();
        descriptionGenerator = new DescriptionGeneratorService();
        // Configurer la clé API DeepInfra
        descriptionGenerator.setApiKey("eceHf7bTVc9wvTsyiuBowZz9u7vrlsMF");

        // Réinitialiser le message de validation
        validationMessageLabel.setText("");
        weatherLabel.setText("Sélectionnez une date et une catégorie pour voir les prévisions");

        // S'assurer que l'icône météo est visible dès le début
        try {
            Image defaultWeatherIcon = new Image(getClass().getResourceAsStream("/images/sunny.png"));
            if (defaultWeatherIcon != null && !defaultWeatherIcon.isError()) {
                weatherIconView.setImage(defaultWeatherIcon);
                System.out.println("Image par défaut chargée avec succès");
            } else {
                System.err.println("Image par défaut non trouvée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
            e.printStackTrace();
        }

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

        // Configurer l'affichage des catégories dans le ComboBox
        categoryField.setCellFactory(lv -> new ListCell<CategoryEvent>() {
            @Override
            protected void updateItem(CategoryEvent category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.getType() + " (à " + category.getLocation() + ")");
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
                    setText(category.getType() + " (à " + category.getLocation() + ")");
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
            if (newValue != null && categoryField.getValue() != null) {
                System.out.println("Date changée, mise à jour météo");
                updateWeatherInfo(newValue, categoryField.getValue().getLocation());
            }
        });

        categoryField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && datePicker.getValue() != null) {
                System.out.println("Catégorie changée, mise à jour météo");
                updateWeatherInfo(datePicker.getValue(), newValue.getLocation());
            }
        });

        // Ajouter un listener pour le champ de titre pour générer la description uniquement quand le focus est perdu
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            // Quand le focus est perdu et que le titre est valide
            if (!newVal && titleField.getText() != null && !titleField.getText().trim().isEmpty()) {
                generateDescriptionFromTitle();
            }
        });

        // Désactiver le bouton de sauvegarde initialement jusqu'à ce que tous les champs soient valides
        saveButton.setDisable(true);
    }

    private void updateWeatherInfo(LocalDate date, String location) {
        if (date == null || location == null) {
            weatherLabel.setText("Sélectionnez une date et une catégorie pour voir les prévisions");
            weatherIconView.setImage(null);
            return;
        }

        try {
            // Afficher un message de chargement
            weatherLabel.setText("Chargement des données météo...");
            weatherIconView.setImage(null);

            // Récupérer les données météo
            WeatherData weatherData = weatherService.getWeatherForDate(date, location);

            if (weatherData != null) {
                // Définir l'icône météo en fonction de la condition
                String imageUrl = "";
                String condition = weatherData.getCondition().toLowerCase();

                if (condition.contains("rain") || condition.contains("pluie")) {
                    imageUrl = "/images/rain.png";
                } else if (condition.contains("cloud") || condition.contains("nuage") || condition.contains("broken")) {
                    imageUrl = "/images/cloudy.png";
                } else if (condition.contains("clear") || condition.contains("clair")) {
                    imageUrl = "/images/sunny.png";
                } else if (condition.contains("snow") || condition.contains("neige")) {
                    imageUrl = "/images/snow.png";
                } else if (condition.contains("mist") || condition.contains("fog")) {
                    imageUrl = "/images/foggy.png";
                } else if (condition.contains("thunder") || condition.contains("orage")) {
                    imageUrl = "/images/storm.png";
                } else {
                    imageUrl = "/images/sunny.png"; // Image par défaut
                }

                System.out.println("Tentative de chargement de l'image: " + imageUrl);

                try {
                    Image weatherIcon = new Image(getClass().getResourceAsStream(imageUrl));
                    if (weatherIcon != null && !weatherIcon.isError()) {
                        weatherIconView.setImage(weatherIcon);
                        System.out.println("Image météo chargée avec succès: " + imageUrl);
                    } else {
                        System.err.println("Erreur: Image météo non trouvée " + imageUrl);
                        // Essayer de charger l'image par défaut
                        Image defaultIcon = new Image(getClass().getResourceAsStream("/images/sunny.png"));
                        weatherIconView.setImage(defaultIcon);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image météo: " + e.getMessage());
                    e.printStackTrace();
                    // Essayer de charger l'image par défaut
                    try {
                        Image defaultIcon = new Image(getClass().getResourceAsStream("/images/sunny.png"));
                        weatherIconView.setImage(defaultIcon);
                    } catch (Exception ex) {
                        System.err.println("Impossible de charger l'image par défaut");
                    }
                }

                // Format le texte pour la météo comme dans la capture d'écran
                String weatherText = String.format("%.1f°C, %s, Humidité: %.0f%% à %s",
                        weatherData.getTemperature(),
                        weatherData.getCondition(),
                        weatherData.getHumidity(),
                        location);

                weatherLabel.setText(weatherText);
                weatherLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0077cc;");
            } else {
                weatherLabel.setText("Données météo non disponibles pour " + location + " le " + date);
                weatherLabel.setStyle("-fx-text-fill: #ff6600;");
                weatherIconView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des données météo: " + e.getMessage());
            e.printStackTrace();
            weatherLabel.setText("Erreur: Impossible de récupérer les données météo");
            weatherLabel.setStyle("-fx-text-fill: red;");
            weatherIconView.setImage(null);
        }
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

        boolean isDuplicate = existingEvents.stream()
                .anyMatch(e -> e.getTitle().equalsIgnoreCase(title.trim()));

        if (isDuplicate) {
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            validationMessageLabel.setText("Ce titre existe déjà. Veuillez choisir un titre unique.");
        } else {
            titleField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            validationMessageLabel.setText("");
            
            // Générer la description si le titre est valide et unique
            if (descriptionArea.getText() == null || descriptionArea.getText().isEmpty() || 
                    descriptionArea.getText().equals("Génération de la description...")) {
                generateDescriptionFromTitle();
            }
        }

        validateAllFields();
    }

    private void validateTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.trim().isEmpty()) {
                timeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                validationMessageLabel.setText("L'heure est obligatoire");
                return;
            }

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
        boolean titleValid = titleField.getText() != null && !titleField.getText().trim().isEmpty() &&
                titleField.getStyle().contains("green");
        boolean descriptionValid = descriptionArea.getText() != null && !descriptionArea.getText().trim().isEmpty();
        boolean categoryValid = categoryField.getValue() != null;
        boolean dateValid = datePicker.getValue() != null &&
                datePicker.getValue().isAfter(LocalDate.now());

        boolean timeValid = false;
        try {
            if (timeField.getText() != null && !timeField.getText().trim().isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime.parse(timeField.getText().trim(), formatter);
                timeValid = true;
            }
        } catch (Exception e) {
            timeValid = false;
        }

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
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destination = Paths.get(UPLOAD_DIR + uniqueFileName);

                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

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

            existingEvents.add(event);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté",
                    "L'événement a été ajouté avec succès.");

            // Fermer la fenêtre d'ajout pour retourner à la liste
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();

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
        datePicker.setValue(null);
        datePicker.setStyle("");
        timeField.clear();
        timeField.setStyle("");
        photoPath = null;
        photoPathLabel.setText("Aucune image sélectionnée");
        validationMessageLabel.setText("");

        // Réinitialiser l'image météo et le texte
        weatherIconView.setImage(null);
        weatherLabel.setText("Sélectionnez une date et une catégorie pour voir les prévisions");
        weatherLabel.setStyle("");

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

    /**
     * Génère la description à partir du titre saisi
     */
    private void generateDescriptionFromTitle() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            // Ne pas générer si le titre est vide
            return;
        }
        
        // Afficher un indicateur de chargement
        descriptionArea.setText("Génération de la description...");
        descriptionArea.setDisable(true);
        
        System.out.println("Génération de description pour l'événement: " + title);
        
        // Générer la description de manière asynchrone
        descriptionGenerator.generateDescription(title)
            .thenAccept(description -> {
                // Mettre à jour l'interface utilisateur dans le thread JavaFX
                Platform.runLater(() -> {
                    if (description != null && !description.isEmpty()) {
                        descriptionArea.setText(description);
                        System.out.println("Description générée avec succès: " + description.substring(0, Math.min(50, description.length())) + "...");
                    } else {
                        descriptionArea.setText("La génération a échoué. Veuillez saisir manuellement une description.");
                        System.err.println("La génération a échoué - résultat vide");
                    }
                    descriptionArea.setDisable(false);
                    validateAllFields();
                });
            })
            .exceptionally(e -> {
                // Gérer les erreurs dans le thread JavaFX
                Platform.runLater(() -> {
                    descriptionArea.setText("");
                    descriptionArea.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de génération de description", 
                            "Impossible de générer la description: " + e.getMessage());
                    System.err.println("Exception lors de la génération: " + e.getMessage());
                });
                return null;
            });
    }

    /**
     * Configure la clé API pour la génération de descriptions
     * @param apiKey La clé API à utiliser
     */
    public void setApiKey(String apiKey) {
        if (descriptionGenerator != null) {
            descriptionGenerator.setApiKey(apiKey);
            System.out.println("Clé API configurée via setApiKey(): " + apiKey);
        } else {
            System.err.println("Erreur: descriptionGenerator est null dans setApiKey()");
        }
    }
}
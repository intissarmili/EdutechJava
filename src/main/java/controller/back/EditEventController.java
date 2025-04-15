package controller.back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.CategoryEvent;
import model.Event;
import service.CategoryEventService;
import service.Eventservice;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class EditEventController implements Initializable {
    @FXML
    private TextField tfTitle;
    @FXML
    private TextArea taDescription;
    @FXML
    private ComboBox<CategoryEvent> cbCategory;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField tfTime;
    @FXML
    private Button btnImage;
    @FXML
    private Label lblImagePath;
    @FXML
    private Button btnUpdate;

    private Eventservice eventService;
    private CategoryEventService categoryService;
    private String imagePath = null;
    private final String UPLOAD_DIR = "uploads/";
    private Event eventToEdit;
    private List<Event> existingEvents;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eventService = new Eventservice();
        categoryService = new CategoryEventService();

        // Charger tous les événements pour vérifier les titres dupliqués
        existingEvents = eventService.getAll();

        // Charger les catégories disponibles
        loadCategories();

        // Configuration du DatePicker pour n'accepter que les dates futures
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                // Désactiver les dates passées (aujourd'hui inclus pour ne permettre que des dates à partir de demain)
                setDisable(empty || date.compareTo(today) <= 0);

                // Style visuel pour montrer les dates désactivées
                if (date.compareTo(today) <= 0) {
                    setStyle("-fx-background-color: #ffc0cb;"); // Rose pâle pour dates passées
                }
            }
        });

        // Configurer le StringConverter pour afficher uniquement l'ID de la catégorie dans le ComboBox
        cbCategory.setConverter(new StringConverter<CategoryEvent>() {
            @Override
            public String toString(CategoryEvent category) {
                if (category == null) {
                    return null;
                }
                return "ID: " + category.getId();
            }

            @Override
            public CategoryEvent fromString(String string) {
                // Cette méthode n'est pas utilisée dans ce contexte
                return null;
            }
        });

        // Validation du titre en temps réel pour vérifier les doublons
        tfTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTitle(newValue);
        });

        // Validation de l'heure en temps réel
        tfTime.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTime(newValue);
        });
    }

    public void initData(Event event) {
        this.eventToEdit = event;

        // Préremplir les champs avec les valeurs existantes
        tfTitle.setText(event.getTitle());

        if (event.getDescription() != null) {
            taDescription.setText(event.getDescription());
        }

        // Sélectionner la catégorie
        selectCategory(event.getCategoryevent_id());

        // Définir la date et l'heure
        dpDate.setValue(event.getDateTime().toLocalDate());
        tfTime.setText(event.getDateTime().toLocalTime().toString());

        // Afficher le chemin de l'image si disponible
        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            imagePath = event.getPhoto();
            File file = new File(imagePath);
            lblImagePath.setText(file.getName());
        }
    }

    private void selectCategory(int categoryId) {
        for (CategoryEvent category : cbCategory.getItems()) {
            if (category.getId() == categoryId) {
                cbCategory.setValue(category);
                break;
            }
        }
    }

    private void loadCategories() {
        try {
            List<CategoryEvent> categories = categoryService.getAll();
            ObservableList<CategoryEvent> categoryList = FXCollections.observableArrayList(categories);
            cbCategory.setItems(categoryList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des catégories",
                    "Impossible de charger les catégories d'événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            tfTitle.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            return;
        }

        // Vérifier si le titre existe déjà pour un autre événement
        boolean isDuplicate = existingEvents.stream()
                .anyMatch(e -> e.getId() != eventToEdit.getId() && e.getTitle().equalsIgnoreCase(title.trim()));

        if (isDuplicate) {
            tfTitle.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.WARNING, "Titre en double", "Ce titre existe déjà",
                    "Veuillez choisir un titre unique pour votre événement.");
        } else {
            tfTitle.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        }

        validateAllFields();
    }

    private void validateTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.trim().isEmpty()) {
                tfTime.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                return;
            }

            // Essayer de parser l'heure
            LocalTime.parse(timeStr.trim());
            tfTime.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } catch (Exception e) {
            tfTime.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }

        validateAllFields();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) btnImage.getScene().getWindow();
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
                imagePath = destination.toString();
                lblImagePath.setText(selectedFile.getName());

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du téléchargement de l'image",
                        e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleUpdate() {
        if (!validateInputs()) {
            return;
        }

        try {
            String title = tfTitle.getText().trim();
            String description = taDescription.getText().trim();
            CategoryEvent selectedCategory = cbCategory.getValue();
            int categoryId = selectedCategory.getId();

            LocalDate date = dpDate.getValue();
            LocalTime time = LocalTime.parse(tfTime.getText()); // Format attendu: HH:MM
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // Vérifier à nouveau si le titre est unique
            boolean isDuplicate = existingEvents.stream()
                    .anyMatch(e -> e.getId() != eventToEdit.getId() && e.getTitle().equalsIgnoreCase(title));

            if (isDuplicate) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Titre en double",
                        "Ce titre existe déjà. Veuillez choisir un titre unique.");
                return;
            }

            // Vérifier à nouveau si la date est future
            if (date.compareTo(LocalDate.now()) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Date invalide",
                        "La date doit être à partir de demain.");
                return;
            }

            // Mettre à jour l'événement avec les nouvelles valeurs
            eventToEdit.setTitle(title);
            eventToEdit.setDescription(description);
            eventToEdit.setCategoryevent_id(categoryId);
            eventToEdit.setDateTime(dateTime);

            if (imagePath != null) {
                eventToEdit.setPhoto(imagePath);
            }

            eventService.update(eventToEdit);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement mis à jour",
                    "L'événement a été mis à jour avec succès.");

            // Fermer la fenêtre après la mise à jour
            closeWindow();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de l'événement",
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (tfTitle.getText().trim().isEmpty()) {
            errors.append("Le titre est obligatoire.\n");
        }

        if (cbCategory.getValue() == null) {
            errors.append("Veuillez sélectionner une catégorie.\n");
        }

        if (dpDate.getValue() == null) {
            errors.append("Veuillez sélectionner une date.\n");
        } else if (dpDate.getValue().compareTo(LocalDate.now()) <= 0) {
            errors.append("La date doit être à partir de demain.\n");
        }

        try {
            if (!tfTime.getText().trim().isEmpty()) {
                LocalTime.parse(tfTime.getText().trim());
            } else {
                errors.append("L'heure est obligatoire.\n");
            }
        } catch (Exception e) {
            errors.append("Format d'heure invalide. Utilisez le format HH:MM.\n");
        }

        // Vérification du titre en doublon
        String title = tfTitle.getText().trim();
        boolean isDuplicate = existingEvents.stream()
                .anyMatch(e -> e.getId() != eventToEdit.getId() && e.getTitle().equalsIgnoreCase(title));

        if (isDuplicate) {
            errors.append("Ce titre existe déjà. Veuillez choisir un titre unique.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez corriger les erreurs suivantes:",
                    errors.toString());
            return false;
        }

        return true;
    }

    private void validateAllFields() {
        boolean titleValid = !tfTitle.getText().trim().isEmpty() && tfTitle.getStyle().contains("green");
        boolean categoryValid = cbCategory.getValue() != null;
        boolean dateValid = dpDate.getValue() != null && dpDate.getValue().compareTo(LocalDate.now()) > 0;
        boolean timeValid = tfTime.getStyle().contains("green");

        // Activer/désactiver le bouton de mise à jour
        btnUpdate.setDisable(!(titleValid && categoryValid && dateValid && timeValid));
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
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) tfTitle.getScene().getWindow();
        stage.close();
    }
}
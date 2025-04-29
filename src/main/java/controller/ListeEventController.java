package controller;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.CategoryEvent;
import model.Event;
import service.CategoryEventService;
import service.Eventservice;
import connect.MyDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import service.WeatherService;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ListeEventController {
    @FXML
    private HBox weatherContainer;
    @FXML
    private ImageView weatherIcon;
    @FXML
    private Label weatherCondition;
    @FXML
    private Label weatherTemperature;
    @FXML
    private DatePicker datePicker;

    @FXML
    private FlowPane eventsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<CategoryEvent> categoryFilterComboBox;
    @FXML
    private ComboBox<CategoryEvent> categoryComboBox;
    @FXML
    private ListView<Event> eventListView;

    private List<Event> allEvents; // Stocke tous les événements non filtrés
    private Eventservice eventService = new Eventservice();
    private CategoryEventService categoryService = new CategoryEventService();
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField scoreField;

    @FXML
    private Button applyFilterButton;

    private CategoryEventService CategoryService = new CategoryEventService();
    private Eventservice service = new Eventservice();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur ListeEventController");

        // Test de connexion à la base de données
        testDatabaseConnection();

        // Configuration de la FlowPane pour afficher 3 cartes par ligne
        eventsContainer.setPrefWrapLength(900); // Ajustez selon la largeur de votre fenêtre
        eventsContainer.setHgap(10);
        eventsContainer.setVgap(10);
        eventsContainer.setPadding(new Insets(15));
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }

        // Initialiser le combo box des catégories
        initCategoryFilter();

        // Rafraîchir la vue au lancement
        refreshTable();
        if (applyFilterButton != null) {
            applyFilterButton.setOnAction(this::handleFilterApply);
        }
    }
    private void initCategoryFilter() {
        // Récupérer toutes les catégories
        List<CategoryEvent> categories = categoryService.getAllCategories();

        // Option "Tous les types"
        CategoryEvent allTypes = new CategoryEvent(0, "Tous les types", "", "");

        // Créer un Set pour stocker les types uniques
        Set<String> uniqueTypes = new HashSet<>();
        for (CategoryEvent category : categories) {
            uniqueTypes.add(category.getType());
        }

        // Créer une liste observable avec l'option "Tous les types" en premier
        ObservableList<CategoryEvent> observableCategories = FXCollections.observableArrayList(allTypes);

        // Ajouter une option pour chaque type unique
        for (String type : uniqueTypes) {
            // ID -1 indique que c'est un filtrage par type et non par ID spécifique
            CategoryEvent typeCategory = new CategoryEvent(-1, type, "", "");
            observableCategories.add(typeCategory);
        }

        // Ajouter toutes les catégories spécifiques avec leurs vrais IDs
        for (CategoryEvent category : categories) {
            observableCategories.add(category);
        }

        // Configurer le ComboBox
        categoryFilterComboBox.setItems(observableCategories);
        categoryFilterComboBox.setValue(allTypes);

        // Définir l'affichage des items dans le ComboBox
        categoryFilterComboBox.setCellFactory(param -> new ListCell<CategoryEvent>() {
            @Override
            protected void updateItem(CategoryEvent item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else if (item.getId() == 0) {
                    setText("Tous les types");
                } else if (item.getId() == -1) {
                    setText("Type: " + item.getType());
                } else {
                    setText(item.getType() + " - " + item.getLocation());
                }
            }
        });
        // Même chose pour l'affichage de la valeur sélectionnée
        categoryFilterComboBox.setButtonCell(new ListCell<CategoryEvent>() {
            @Override
            protected void updateItem(CategoryEvent item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else if (item.getId() == 0) {
                    setText("Tous les types");
                } else if (item.getId() == -1) {
                    setText("Type: " + item.getType());
                } else {
                    setText(item.getType() + " - " + item.getLocation());
                }
            }
        });

        // Configurer l'action à effectuer lorsqu'une catégorie est sélectionnée

        // Même chose pour l'affichage de la valeur sélectionnée
        categoryFilterComboBox.setButtonCell(new ListCell<CategoryEvent>() {
            @Override
            protected void updateItem(CategoryEvent item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else if (item.getId() == 0) {
                    setText("Toutes les catégories");
                } else {
                    setText(item.getType() + " - " + item.getLocation());
                }
            }
        });
        categoryFilterComboBox.setOnAction(e -> handleFilterApply(e));

    }

    @FXML


    // Méthode pour calculer le score d'un événement (à adapter selon vos critères)
    private int getEventScore(Event event) {
        int score = 0;

        // Exemple : score basé sur divers critères
        if (event.getTitle() != null && !event.getTitle().isEmpty()) {
            score += 2;
        }

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            score += event.getDescription().length() > 100 ? 3 : 1;
        }

        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            score += 2;
        }

        // Ajouter des points selon la catégorie (exemple)
        CategoryEvent category = categoryService.getById(event.getCategoryevent_id());
        if (category != null && "Premium".equals(category.getType())) {
            score += 3;
        }

        return score;
    }

    // Méthode pour afficher les événements filtrés






    @FXML

    private void handleFilterApply(ActionEvent event) {
        CategoryEvent selectedCategory = categoryFilterComboBox.getValue();
        String searchTerm = searchField.getText().toLowerCase().trim();
        String scoreText = scoreField.getText().trim();
        int minimumScore = -1;

        // Vérifier si un score minimum a été spécifié
        if (!scoreText.isEmpty()) {
            try {
                minimumScore = Integer.parseInt(scoreText);
            } catch (NumberFormatException e) {
                showErrorAlert("Erreur", "Score invalide", "Veuillez entrer un nombre entier valide pour le score.");
                return;
            }
        }

        // Récupérer tous les événements
        List<Event> allEvents = service.getAll();
        List<Event> filteredEvents = new ArrayList<>();

        // Appliquer les filtres
        for (Event e : allEvents) {
            boolean matchesCategory = false;

            // Si "Tous les types" est sélectionné ou si la catégorie est vide
            if (selectedCategory == null || selectedCategory.getId() == 0) {
                matchesCategory = true;
            } else {
                // Récupérer la catégorie de l'événement
                CategoryEvent eventCategory = categoryService.getById(e.getCategoryevent_id());

                if (eventCategory != null) {
                    if (selectedCategory.getId() == -1) {
                        // Filtrage par type
                        matchesCategory = eventCategory.getType().equals(selectedCategory.getType());
                    } else {
                        // Filtrage par ID spécifique
                        matchesCategory = eventCategory.getId() == selectedCategory.getId();
                    }
                }
            }

            // Filtrage par texte de recherche
            boolean matchesSearch = searchTerm.isEmpty() ||
                    (e.getTitle() != null && e.getTitle().toLowerCase().contains(searchTerm)) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchTerm));

            // Filtrage par score
            boolean matchesScore = minimumScore == -1 || getEventScore(e) >= minimumScore;

            if (matchesCategory && matchesSearch && matchesScore) {
                filteredEvents.add(e);
            }
        }

        // Obtenir un nom descriptif pour le filtre appliqué
        String filterDescription = "";
        if (selectedCategory != null) {
            if (selectedCategory.getId() == 0) {
                filterDescription = "Tous les types";
            } else if (selectedCategory.getId() == -1) {
                filterDescription = "Type: " + selectedCategory.getType();
            } else {
                filterDescription = selectedCategory.getType() + " - " + selectedCategory.getLocation();
            }
        }

        // Afficher les résultats filtrés
        displayFilteredEvents(filteredEvents, searchTerm, selectedCategory, minimumScore);
    }
    // Méthode pour afficher les événements filtrés
    private void displayFilteredEvents(List<Event> events, String searchTerm, CategoryEvent category, int minimumScore) {
        // Effacer le conteneur
        eventsContainer.getChildren().clear();

        // Si aucun événement ne correspond aux critères
        if (events.isEmpty()) {
            VBox emptyMessage = new VBox();
            emptyMessage.setAlignment(Pos.CENTER);
            emptyMessage.setPrefWidth(eventsContainer.getPrefWidth());
            emptyMessage.setPrefHeight(300);

            Label emptyLabel = new Label("Aucun événement ne correspond aux critères de recherche");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");

            Button clearButton = new Button("Effacer les filtres");
            clearButton.setOnAction(e -> {
                searchField.clear();
                scoreField.clear();
                categoryFilterComboBox.setValue(categoryFilterComboBox.getItems().get(0)); // Première option (Toutes)
                refreshTable();
            });

            emptyMessage.getChildren().addAll(emptyLabel, new Region() {{
                setPrefHeight(20);
            }}, clearButton);
            eventsContainer.getChildren().add(emptyMessage);

            statusLabel.setText("Aucun résultat trouvé");
        } else {
            // Trier les événements par score (décroissant) si un score minimum est spécifié
            if (minimumScore >= 0) {
                events.sort((e1, e2) -> Integer.compare(getEventScore(e2), getEventScore(e1)));
            }

            // Créer une carte pour chaque événement filtré
            for (Event e : events) {
                VBox eventCard = createEventCard(e);
                eventsContainer.getChildren().add(eventCard);
            }

            // Construire un message de statut
            String statusMessage = events.size() + " événement(s) trouvé(s)";
            if (!searchTerm.isEmpty()) {
                statusMessage += " pour \"" + searchTerm + "\"";
            }
            if (category.getId() != 0) {
                statusMessage += " dans la catégorie \"" + category.getType() + "\"";
            }
            if (minimumScore >= 0) {
                statusMessage += " avec un score ≥ " + minimumScore;
            }

            statusLabel.setText(statusMessage);
        }
    }

    private void testDatabaseConnection() {
        System.out.println("Test de connexion à la base de données...");
        try {
            Connection conn = MyDatabase.getInstance().getCnx();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion à la base de données établie avec succès!");

                // Vérifier si la table existe et contient des données
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM event")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("La table 'event' contient " + count + " enregistrements.");
                        statusLabel.setText("Base de données: " + count + " événements disponibles");
                    }
                }
            } else {
                System.err.println("❌ La connexion à la base de données a échoué!");
                statusLabel.setText("Erreur: Impossible de se connecter à la base de données");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du test de connexion: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    // Création d'une carte pour un événement (taille standardisée)
    private VBox createEventCard(Event event) {
        VBox card = new VBox(8); // Réduit l'espacement vertical
        card.getStyleClass().add("event-card");

        // Taille réduite pour les cartes
        card.setPrefWidth(220); // Largeur réduite
        card.setMaxWidth(220);
        card.setMinHeight(350); // Hauteur réduite
        card.setPadding(new Insets(10)); // Padding réduit

        // Récupérer la catégorie
        CategoryEvent category = categoryService.getById(event.getCategoryevent_id());
        String categoryName = category != null ? category.getType() : "Catégorie " + event.getCategoryevent_id();

        // Titre de l'événement
        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Taille de police réduite

        // Date et heure
        Label dateLabel = new Label(event.getDateTime().format(formatter));
        dateLabel.getStyleClass().add("event-date");
        dateLabel.setStyle("-fx-font-size: 12px;"); // Taille de police réduite

        // Catégorie
        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("event-category");
        categoryLabel.setStyle("-fx-font-size: 12px;"); // Taille de police réduite

        // Score
        int score = getEventScore(event);
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.getStyleClass().add("event-score");
        scoreLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

        // Image de taille réduite
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("event-image-container");
        imageContainer.setMinHeight(120); // Hauteur réduite
        imageContainer.setMaxHeight(120);
        imageContainer.setPrefHeight(120);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-radius: 5;");

        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            try {
                File file = new File(event.getPhoto());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(), 200, 120, true, true); // Dimensions réduites
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(120);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);
                    imageContainer.getChildren().add(imageView);
                    StackPane.setAlignment(imageView, Pos.CENTER);
                } else {
                    Text noImageText = new Text("Image non disponible");
                    imageContainer.getChildren().add(noImageText);
                }
            } catch (Exception e) {
                Text errorText = new Text("Erreur de chargement");
                imageContainer.getChildren().add(errorText);
            }
        } else {
            Text noImageText = new Text("Aucune image");
            imageContainer.getChildren().add(noImageText);
        }

        // Description plus courte
        String descriptionText = event.getDescription();
        if (descriptionText != null && !descriptionText.isEmpty()) {
            if (descriptionText.length() > 70) { // Limite réduite
                descriptionText = descriptionText.substring(0, 67) + "...";
            }
        } else {
            descriptionText = "Aucune description disponible";
        }

        Label descriptionLabel = new Label(descriptionText);
        descriptionLabel.getStyleClass().add("event-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);
        descriptionLabel.setMaxHeight(40); // Hauteur réduite
        descriptionLabel.setStyle("-fx-font-size: 12px;"); // Taille de police réduite

        // Boutons d'action simplifiés en une seule ligne
        HBox actionButtons = new HBox(5);
        actionButtons.setAlignment(Pos.CENTER);

        Button btnView = new Button("👁️");
        btnView.getStyleClass().add("btn-icon");
        btnView.setTooltip(new Tooltip("Voir détails"));

        Button btnEdit = new Button("✏️");
        btnEdit.getStyleClass().add("btn-icon");
        btnEdit.setTooltip(new Tooltip("Modifier"));

        Button btnQR = new Button("📱");
        btnQR.getStyleClass().add("btn-icon");
        btnQR.setTooltip(new Tooltip("QR Code"));

        Button btnDelete = new Button("🗑️");
        btnDelete.getStyleClass().add("btn-icon");
        btnDelete.setTooltip(new Tooltip("Supprimer"));

        actionButtons.getChildren().addAll(btnView, btnEdit, btnQR, btnDelete);

        // Ajouter les actions aux boutons
        btnView.setOnAction(e -> showDetailDialog(event));
        btnEdit.setOnAction(e -> showEditDialog(event));
        btnQR.setOnAction(e -> generateQRCode(event));
        btnDelete.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir supprimer cet événement?",
                    ButtonType.YES, ButtonType.NO);
            confirmDialog.setTitle("Confirmation de suppression");
            confirmDialog.setHeaderText("Supprimer l'événement: " + event.getTitle());
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    service.deleteById(event.getId());
                    refreshTable();
                    showSuccessAlert("Événement supprimé avec succès");
                }
            });
        });

        // Organiser l'en-tête avec titre et catégorie
        VBox header = new VBox(2); // Espacement réduit
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(titleLabel, dateLabel, categoryLabel, scoreLabel);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(
                header,
                imageContainer,
                descriptionLabel,
                new Separator(),
                actionButtons
        );

        return card;
    }

    // Rafraîchir la vue avec les événements mis à jour
    @FXML
    public void refreshTable() {
        System.out.println("Rafraîchissement de la vue...");
        List<Event> events = service.getAll();
        System.out.println("Mise à jour de la vue avec " + events.size() + " événements");

        // Effacer le conteneur
        eventsContainer.getChildren().clear();

        // Si aucun événement n'est disponible, afficher un message
        if (events.isEmpty()) {
            System.out.println("⚠️ Aucun événement récupéré de la base de données");

            VBox emptyMessage = new VBox();
            emptyMessage.setAlignment(Pos.CENTER);
            emptyMessage.setPrefWidth(eventsContainer.getPrefWidth());
            emptyMessage.setPrefHeight(300);

            Label emptyLabel = new Label("Aucun événement disponible");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");

            Button addButton = new Button("Ajouter un événement");
            addButton.getStyleClass().add("add-button");
            addButton.setOnAction(this::openAddForm);

            emptyMessage.getChildren().addAll(emptyLabel, new Region() {{
                setPrefHeight(20);
            }}, addButton);
            eventsContainer.getChildren().add(emptyMessage);

            statusLabel.setText("Aucun événement disponible");
        } else {
            // Créer une carte pour chaque événement
            for (Event event : events) {
                VBox eventCard = createEventCard(event);
                eventsContainer.getChildren().add(eventCard);
            }

            statusLabel.setText(events.size() + " événements affichés");
            System.out.println("Liste des événements chargés avec succès");
        }
    }

    // Les autres méthodes restent inchangées...
    // Ouvrir le formulaire d'ajout d'événement
    @FXML
    private void openAddForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/AjouterEvent.fxml"));
            Parent root = loader.load();
            
            // Get the controller and initialize if needed
            controller.back.AjouterEventController controller = loader.getController();
            // Make sure the DeepInfra API key is set
            controller.setApiKey("eceHf7bTVc9wvTsyiuBowZz9u7vrlsMF");

            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh après ajout
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", e.getMessage());
        }
    }

    // Ouvrir le formulaire d'édition d'événement
    private void showEditDialog(Event ev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/EditEventView.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et initialiser avec l'événement à modifier
            EditEventController controller = loader.getController();
            controller.initData(ev);

            Stage stage = new Stage();
            stage.setTitle("Modifier un événement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Après la fermeture de la fenêtre, rafraîchir la liste
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire d'édition", e.getMessage());
        }
    }
    // Afficher les détails d'un événement
    private void showDetailDialog(Event ev) {
        try {
            // Créer une boîte de dialogue avec les détails
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Détails de l'événement");
            dialog.setHeaderText(ev.getTitle());

            // Configurer le contenu de la boîte de dialogue
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setMaxWidth(500);

            // Image
            if (ev.getPhoto() != null && !ev.getPhoto().isEmpty()) {
                try {
                    File file = new File(ev.getPhoto());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString(), 450, 300, true, true);
                        ImageView imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(450);

                        // Ajouter un StackPane pour centrer l'image
                        StackPane imagePane = new StackPane(imageView);
                        imagePane.setStyle("-fx-padding: 10; -fx-background-color: #f5f7fa; -fx-background-radius: 5;");
                        content.getChildren().add(imagePane);
                    }
                } catch (Exception e) {
                    // Ne pas ajouter d'image en cas d'erreur
                }
            }

            // Informations
            VBox infoBox = new VBox(10);

            // Titre avec style
            Label titleLabel = new Label(ev.getTitle());
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            // Date et heure
            HBox dateBox = new HBox(10);
            Label dateLabel = new Label("Date et heure:");
            dateLabel.setStyle("-fx-font-weight: bold;");
            Label dateValue = new Label(ev.getDateTime().format(formatter));
            dateBox.getChildren().addAll(dateLabel, dateValue);

            // Catégorie
            HBox categoryBox = new HBox(10);
            Label categoryLabel = new Label("Catégorie:");
            categoryLabel.setStyle("-fx-font-weight: bold;");
            Label categoryValue = new Label(String.valueOf(ev.getCategoryevent_id()));
            categoryBox.getChildren().addAll(categoryLabel, categoryValue);

            // Description
            VBox descriptionBox = new VBox(5);
            Label descriptionLabel = new Label("Description:");
            descriptionLabel.setStyle("-fx-font-weight: bold;");

            TextFlow descriptionFlow = new TextFlow();
            String descriptionText = ev.getDescription() != null ? ev.getDescription() : "Aucune description disponible";
            Text descriptionValue = new Text(descriptionText);
            descriptionFlow.getChildren().add(descriptionValue);
            descriptionFlow.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

            descriptionBox.getChildren().addAll(descriptionLabel, descriptionFlow);

            infoBox.getChildren().addAll(titleLabel, dateBox, categoryBox, descriptionBox);
            content.getChildren().add(infoBox);

            // Configurer le dialogue
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'afficher les détails", e.getMessage());
        }
    }

    // Génération de code QR pour un événement
    private void generateQRCode(Event event) {
        try {
            // Créer le contenu du QR code
            String qrContent = "EVENT:" + event.getId() +
                    "\nTITLE:" + event.getTitle() +
                    "\nDATE:" + event.getDateTime().format(formatter) +
                    "\nCATEGORY:" + event.getCategoryevent_id();

            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                // Limiter la description pour éviter un QR code trop dense
                String shortDesc = event.getDescription();
                if (shortDesc.length() > 100) {
                    shortDesc = shortDesc.substring(0, 97) + "...";
                }
                qrContent += "\nDESC:" + shortDesc;
            }

            // Configuration du QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            // Générer la matrice du QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    300, 300,
                    hints
            );

            // Convertir en image
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            ByteArrayInputStream pngInputStream = new ByteArrayInputStream(pngOutputStream.toByteArray());
            Image qrImage = new Image(pngInputStream);

            // Afficher l'image du QR code dans une boîte de dialogue
            showQRCodeDialog(event, qrImage);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de génération", "Impossible de générer le code QR", e.getMessage());
        }
    }

    // Méthode pour afficher le QR code dans une boîte de dialogue
    private void showQRCodeDialog(Event event, Image qrImage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Code QR pour l'événement");
        dialog.setHeaderText("Scannez ce code pour accéder aux détails de : " + event.getTitle());

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        // Affichage de l'image QR
        ImageView qrImageView = new ImageView(qrImage);
        qrImageView.setFitWidth(300);
        qrImageView.setFitHeight(300);

        // Informations sur l'événement
        Label infoLabel = new Label("ID: " + event.getId() + "\nTitre: " + event.getTitle());
        infoLabel.setStyle("-fx-font-weight: bold;");

        Label instructionLabel = new Label("Ce code QR contient les informations essentielles de l'événement.");
        instructionLabel.setWrapText(true);
        instructionLabel.setStyle("-fx-font-style: italic;");

        // Bouton pour enregistrer l'image
        Button saveButton = new Button("Enregistrer l'image");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> {
            try {
                // Créer le répertoire qrcodes s'il n'existe pas
                File qrDir = new File("qrcodes");
                if (!qrDir.exists()) {
                    qrDir.mkdir();
                }

                // Créer un fichier pour le QR code
                String fileName = "qrcodes/event_" + event.getId() + "_qr.png";
                File qrFile = new File(fileName);

                // Enregistrer l'image - version corrigée sans SwingFXUtils
                BufferedImage bufferedImage = new BufferedImage(
                        (int) qrImage.getWidth(),
                        (int) qrImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                // Alternative: Utiliser directement MatrixToImageWriter pour sauvegarder dans un fichier
                BitMatrix bitMatrix = generateQRBitMatrix(event);
                if (bitMatrix != null) {
                    MatrixToImageWriter.writeToPath(
                            bitMatrix,
                            "PNG",
                            qrFile.toPath()
                    );
                    showSuccessAlert("QR Code enregistré avec succès: " + qrFile.getAbsolutePath());
                } else {
                    throw new IOException("Impossible de générer la matrice QR");
                }
            } catch (IOException | WriterException ex) {
                showErrorAlert("Erreur", "Impossible d'enregistrer l'image", ex.getMessage());
            }
        });

        content.getChildren().addAll(qrImageView, infoLabel, instructionLabel, saveButton);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    // Méthode utilitaire pour générer la matrice BitMatrix du QR code
    private BitMatrix generateQRBitMatrix(Event event) throws WriterException {
        // Créer le contenu du QR code
        String qrContent = "EVENT:" + event.getId() +
                "\nTITLE:" + event.getTitle() +
                "\nDATE:" + event.getDateTime().format(formatter) +
                "\nCATEGORY:" + event.getCategoryevent_id();

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            // Limiter la description pour éviter un QR code trop dense
            String shortDesc = event.getDescription();
            if (shortDesc.length() > 100) {
                shortDesc = shortDesc.substring(0, 97) + "...";
            }
            qrContent += "\nDESC:" + shortDesc;
        }

        // Configuration du QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);

        // Générer la matrice du QR code
        return qrCodeWriter.encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                300, 300,
                hints
        );
    }

    // Afficher une alerte de succès
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Afficher une alerte d'erreur
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            refreshTable();  // Si la recherche est vide, afficher tous les événements
            return;
        }

        List<Event> allEvents = service.getAll();
        List<Event> filteredEvents = new java.util.ArrayList<>();

        // Filtrer les événements selon les critères de recherche
        for (Event e : allEvents) {
            if ((e.getTitle() != null && e.getTitle().toLowerCase().contains(searchTerm)) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchTerm)) ||
                    String.valueOf(e.getCategoryevent_id()).equals(searchTerm)) {
                filteredEvents.add(e);
            }
        }

        // Effacer le conteneur
        eventsContainer.getChildren().clear();

        // Afficher les résultats filtrés
        if (filteredEvents.isEmpty()) {
            VBox emptyMessage = new VBox();
            emptyMessage.setAlignment(Pos.CENTER);
            emptyMessage.setPrefWidth(eventsContainer.getPrefWidth());
            emptyMessage.setPrefHeight(300);

            Label emptyLabel = new Label("Aucun résultat pour \"" + searchTerm + "\"");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6;");

            Button clearButton = new Button("Effacer la recherche");
            clearButton.setOnAction(e -> {
                searchField.clear();
                refreshTable();
            });

            emptyMessage.getChildren().addAll(emptyLabel, new Region() {{
                setPrefHeight(20);
            }}, clearButton);
            eventsContainer.getChildren().add(emptyMessage);
        } else {
            // Créer une carte pour chaque événement filtré
            for (Event e : filteredEvents) {
                VBox eventCard = createEventCard(e);
                eventsContainer.getChildren().add(eventCard);
            }
        }

        statusLabel.setText(filteredEvents.size() + " résultats trouvés pour \"" + searchTerm + "\"");
        System.out.println("Recherche '" + searchTerm + "' - " + filteredEvents.size() + " résultats trouvés");
    }

    // Exporter la liste des événements


    // Exporter la liste des événements
    @FXML
    private void handleExport(ActionEvent event) {
        List<Event> events = service.getAll();

        if (events.isEmpty()) {
            showErrorAlert("Export impossible", "Aucune donnée à exporter", "La liste des événements est vide.");
            return;
        }

        try {
            // Créer un fichier CSV
            File exportFile = new File("events_export_" + System.currentTimeMillis() + ".csv");
            java.io.PrintWriter writer = new java.io.PrintWriter(exportFile);

            // Écrire l'en-tête
            writer.println("ID,Titre,Description,Catégorie,Date et Heure,Photo");

            // Écrire les données
            for (Event e : events) {
                writer.println(
                        e.getId() + "," +
                                csvEscape(e.getTitle()) + "," +
                                csvEscape(e.getDescription()) + "," +
                                e.getCategoryevent_id() + "," +
                                e.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," +
                                csvEscape(e.getPhoto())
                );
            }

            writer.close();

            showSuccessAlert("Export réussi! Fichier sauvegardé: " + exportFile.getAbsolutePath());
        } catch (Exception e) {
            showErrorAlert("Erreur d'export", "Impossible d'exporter les données", e.getMessage());
        }
    }

    // Échapper les virgules et les guillemets pour le CSV
    private String csvEscape(String value) {
        if (value == null) return "";

        // Échapper les guillemets en les doublant et entourer de guillemets si nécessaire
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    // Ajoutez cette méthode à votre classe ListeEventController
}

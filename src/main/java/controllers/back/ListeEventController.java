package controllers.back;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.application.Platform;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.CategoryEvent;
import models.Event;
import service.CategoryEventService;
import service.Eventservice;
import utils.MyDatabase;
import utils.EventBus;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.layout.HBox;
import service.WeatherService;
import models.WeatherData;
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

    // Nouveaux éléments pour le carousel avec 3 images côte à côte
    @FXML
    private VBox carouselContainer;
    @FXML
    private HBox imageGroup1;
    @FXML
    private HBox imageGroup2;
    @FXML
    private HBox imageGroup3;

    // Images du premier groupe
    @FXML
    private ImageView eventImage1;
    @FXML
    private ImageView eventImage2;
    @FXML
    private ImageView eventImage3;

    // Images du deuxième groupe
    @FXML
    private ImageView eventImage4;
    @FXML
    private ImageView eventImage5;
    @FXML
    private ImageView eventImage6;

    // Images du troisième groupe
    @FXML
    private ImageView eventImage7;
    @FXML
    private ImageView eventImage8;
    @FXML
    private ImageView eventImage9;
    @FXML
    private Button carouselDot1;
    @FXML
    private Button carouselDot2;
    @FXML
    private Button carouselDot3;
    @FXML
    private Button carouselPrev;
    @FXML
    private Button carouselNext;

    // Nouveau conteneur GridPane au lieu de FlowPane
    @FXML
    private GridPane eventsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<CategoryEvent> categoryFilterComboBox;
    @FXML
    private ComboBox<String> statusFilterComboBox; // Pour les statuts (String)
    @FXML
    private ListView<Event> eventListView;
    @FXML
    private VBox advancedFilters;

    @FXML
    private GridPane filtersGrid;

    @FXML
    private Label toggleFiltersLabel;

    @FXML
    private Label toggleArrow;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private ComboBox<String> orderComboBox;

    private List<Event> allEvents; // Stocke tous les événements non filtrés
    private Eventservice eventService = new Eventservice();
    private CategoryEventService categoryService = new CategoryEventService();
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField scoreFilterField;

    @FXML
    private TextField scoreField;

    @FXML
    private Button applyFilterButton;

    // Variables pour le carousel d'événements
    private int currentCarouselIndex = 0;
    private List<HBox> carouselGroups = new ArrayList<>();
    private List<Button> carouselDots = new ArrayList<>();

    // Contrôles pour le carousel météo
    @FXML
    private ComboBox<CategoryEvent> carouselCategoryFilterComboBox;
    @FXML
    private DatePicker carouselDatePicker;
    @FXML
    private ComboBox<String> carouselSortComboBox;
    @FXML
    private ComboBox<String> carouselOrderComboBox;

    // Variables pour le carousel météo
    private int currentWeatherIndex = 0;
    private final List<VBox> weatherItems = new ArrayList<>();
    private final List<Button> weatherDots = new ArrayList<>();

    // Références aux éléments du carousel météo
    @FXML
    private StackPane weatherCarouselContainer;
    @FXML
    private VBox weatherImage1;
    @FXML
    private VBox weatherImage2;
    @FXML
    private VBox weatherImage3;
    @FXML
    private ImageView weatherImg1;
    @FXML
    private ImageView weatherImg2;
    @FXML
    private ImageView weatherImg3;
    @FXML
    private Button weatherDot1;
    @FXML
    private Button weatherDot2;
    @FXML
    private Button weatherDot3;
    @FXML
    private Button weatherPrev;
    @FXML
    private Button weatherNext;

    private CategoryEventService CategoryService = new CategoryEventService();
    private Eventservice service = new Eventservice();
    private WeatherService weatherService = new WeatherService();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Variables pour l'animation du carousel horizontal
    private Timer carouselTimer;

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur ListeEventController");

        try {
            // Test de connexion à la base de données
            testDatabaseConnection();

            // Configuration du ScrollPane
            if (scrollPane != null) {
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }

            // Initialiser les options de tri
            initSortOptions();

            // Initialiser les filtres de catégorie
            initCategoryFilter();

            // S'abonner à l'événement d'ajout d'événement
            EventBus.getInstance().subscribe("event_added", (eventType, data) -> {
                System.out.println("Nouvel événement ajouté, rafraîchissement de la liste");
                Platform.runLater(this::refreshTable);
            });

            // Initialiser le carousel météo
            initWeatherCarousel();

            // Charger les événements d'abord pour avoir des données disponibles
            refreshTable();

            // Initialiser le carousel avec les images des événements
            // Mettre l'initialisation du carousel dans un Platform.runLater pour éviter les erreurs de threading
            Platform.runLater(() -> {
                initCarousel();
            });

            // Synchroniser les filtres entre le haut de la page et le carousel
            initFilterSynchronization();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du contrôleur: " + e.getMessage());
            e.printStackTrace();
        }

        // Vérifier si le bouton de filtre est présent
        if (applyFilterButton != null) {
            applyFilterButton.setOnAction(this::handleFilterApply);
        }

        // Configuration du bouton de recherche
        searchField.setOnAction(this::handleFilterApply);
    }

    /**
     * Initialise les options de tri avec plus de choix
     */
    private void initSortOptions() {
        // Options de tri
        List<String> sortOptions = Arrays.asList(
                "Date",
                "Prix",
                "Nom (alphabétique)",
                "Score",
                "Catégorie"
        );

        // Initialiser le ComboBox principal
        if (sortComboBox != null) {
            sortComboBox.getItems().clear();
            sortComboBox.getItems().addAll(sortOptions);
            sortComboBox.setValue("Date");
        }

        // Options d'ordre
        if (orderComboBox != null) {
            orderComboBox.getItems().clear();
            orderComboBox.getItems().addAll(
                    "Croissant",
                    "Décroissant"
            );
            orderComboBox.setValue("Décroissant");
        }

        // Options de statut
        if (statusFilterComboBox != null) {
            statusFilterComboBox.getItems().clear();
            statusFilterComboBox.getItems().addAll(
                    "Tous les événements",
                    "À venir",
                    "Passés",
                    "Actif",
                    "Terminé",
                    "Annulé",
                    "Reporté"
            );
            statusFilterComboBox.setValue("Tous les événements");
        }

        // Initialiser le champ de score minimum
        if (scoreField != null) {
            scoreField.setText("0");
        }
    }

    /**
     * Initialise le carousel avec les images des événements
     */
    private void initCarousel() {
        // Ajouter les groupes d'images du carousel aux listes
        carouselGroups.add(imageGroup1);
        carouselGroups.add(imageGroup2);
        carouselGroups.add(imageGroup3);

        carouselDots.add(carouselDot1);
        carouselDots.add(carouselDot2);
        carouselDots.add(carouselDot3);

        // Charger les images des événements pour le carousel
        loadCarouselImages();

        // Configurer les actions des boutons de navigation
        carouselPrev.setOnAction(event -> previousCarouselImage());
        carouselNext.setOnAction(event -> nextCarouselImage());

        // Configurer les actions des indicateurs (dots)
        carouselDot1.setOnAction(event -> showCarouselImage(0));
        carouselDot2.setOnAction(event -> showCarouselImage(1));
        carouselDot3.setOnAction(event -> showCarouselImage(2));

        // Afficher le premier groupe d'images par défaut
        showCarouselImage(0);

        // Démarrer l'animation automatique du carousel
        startCarouselAnimation();
    }

    /**
     * Charge les images d'événements pour le carousel (9 images au total)
     */
    private void loadCarouselImages() {
        List<Event> allEvents = service.getAll();

        // Si nous avons des événements
        if (allEvents != null && !allEvents.isEmpty()) {
            // Trier par date (plus récents en premier)
            allEvents.sort((e1, e2) -> e2.getDateTime().compareTo(e1.getDateTime()));

            // Charger les images dans chaque groupe (jusqu'à 9 images)
            int totalSlots = 9;
            int count = Math.min(allEvents.size(), totalSlots);

            // Tableau des ImageViews pour faciliter l'affectation
            ImageView[] imageViews = {
                    eventImage1, eventImage2, eventImage3,
                    eventImage4, eventImage5, eventImage6,
                    eventImage7, eventImage8, eventImage9
            };

            // Remplir les emplacements disponibles avec des images d'événements
            for (int i = 0; i < count; i++) {
                Event event = allEvents.get(i);
                setEventImage(imageViews[i], event);
            }

            // Remplir les emplacements restants avec des images par défaut
            for (int i = count; i < totalSlots; i++) {
                setDefaultImage(imageViews[i]);
            }
        } else {
            // Pas d'événements, mettre des images par défaut pour tous les emplacements
            ImageView[] imageViews = {
                    eventImage1, eventImage2, eventImage3,
                    eventImage4, eventImage5, eventImage6,
                    eventImage7, eventImage8, eventImage9
            };

            for (ImageView imageView : imageViews) {
                setDefaultImage(imageView);
            }
        }
    }

    /**
     * Définit l'image d'un événement pour un ImageView spécifique
     */
    private void setEventImage(ImageView imageView, Event event) {
        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            try {
                File file = new File(event.getPhoto());
                if (file.exists()) {
                    // Utiliser les nouvelles dimensions adaptées pour l'image horizontale
                    Image image = new Image(file.toURI().toString(), 328, 250, false, true);
                    imageView.setImage(image);

                    // Ajouter une classe pour le style
                    imageView.getStyleClass().add("event-image");

                    // Ajouter un style pour les bordures et arrondir les coins
                    imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2); -fx-background-radius: 8;");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }

        // En cas d'erreur ou si l'image n'existe pas, utiliser une image par défaut
        setDefaultImage(imageView);
    }

    /**
     * Définit une image par défaut pour un ImageView spécifique
     */
    private void setDefaultImage(ImageView imageView) {
        try {
            // Essayer de charger une image par défaut depuis les ressources
            String defaultImagePath = "/images/default-event.png";
            Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));

            if (defaultImage != null && !defaultImage.isError()) {
                imageView.setImage(defaultImage);
            } else {
                // Fallback si l'image par défaut n'est pas disponible
                imageView.setImage(null);
                imageView.setStyle("-fx-background-color: #e3e3e3;");
            }
        } catch (Exception e) {
            // Fallback en cas d'erreur
            imageView.setImage(null);
            imageView.setStyle("-fx-background-color: #e3e3e3;");
        }
    }

    /**
     * Affiche le groupe d'images spécifié dans le carousel
     */
    private void showCarouselImage(int index) {
        if (index < 0 || index >= carouselGroups.size()) return;

        currentCarouselIndex = index;

        // Cacher tous les groupes d'images
        for (int i = 0; i < carouselGroups.size(); i++) {
            carouselGroups.get(i).setVisible(false);
            carouselDots.get(i).getStyleClass().remove("carousel-dot-active");
        }

        // Afficher le groupe d'images sélectionné
        carouselGroups.get(index).setVisible(true);
        carouselDots.get(index).getStyleClass().add("carousel-dot-active");
    }

    /**
     * Méthodes spécifiques pour chaque image du carousel (référencées dans le FXML)
     */
    /**
     * Affiche le premier groupe d'images
     */
    @FXML
    public void showCarouselImage1() {
        showCarouselImage(0);
    }

    /**
     * Affiche le deuxième groupe d'images
     */
    @FXML
    public void showCarouselImage2() {
        showCarouselImage(1);
    }

    /**
     * Affiche le troisième groupe d'images
     */
    @FXML
    public void showCarouselImage3() {
        showCarouselImage(2);
    }

    /**
     * Affiche le groupe d'images précédent dans le carousel
     */
    @FXML
    public void previousCarouselImage() {
        int newIndex = (currentCarouselIndex - 1 + carouselGroups.size()) % carouselGroups.size();
        showCarouselImage(newIndex);
    }

    /**
     * Affiche le groupe d'images suivant dans le carousel
     */
    @FXML
    public void nextCarouselImage() {
        currentCarouselIndex = (currentCarouselIndex + 1) % carouselGroups.size();
        showCarouselImage(currentCarouselIndex);
    }

    /**
     * Démarre l'animation automatique du carousel pour la pagination horizontale
     */
    private void startCarouselAnimation() {
        try {
            // Arrêter le timer existant s'il y en a un
            if (carouselTimer != null) {
                carouselTimer.cancel();
                carouselTimer.purge();
                carouselTimer = null;
            }

            // Créer un nouveau timer qui change le groupe d'images toutes les 5 secondes
            carouselTimer = new Timer(true); // Utiliser un daemon timer
            carouselTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Platform.runLater(() -> nextCarouselImage());
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'animation du carousel: " + e.getMessage());
                    }
                }
            }, 5000, 5000); // Délai initial et intervalle en millisecondes

            System.out.println("Animation du carousel démarrée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'animation du carousel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //---------- Méthodes pour le carousel d'images météo ----------//

    /**
     * Initialise le carousel d'images météo
     */
    private void initWeatherCarousel() {
        try {
            // Ajouter les images au carousel
            weatherItems.add(weatherImage1);
            weatherItems.add(weatherImage2);
            weatherItems.add(weatherImage3);

            // Ajouter les points de pagination
            weatherDots.add(weatherDot1);
            weatherDots.add(weatherDot2);
            weatherDots.add(weatherDot3);

            // Initialiser la synchronisation des filtres entre les différentes vues
            initFilterSynchronization();

            // Afficher la première image
            showWeatherImage(0);

            // Démarrer l'animation automatique du carousel
            startWeatherCarouselAnimation();

            // Configurer le ScrollPane pour permettre le défilement sur toute la page
            if (scrollPane != null) {
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                scrollPane.setPannable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'initialisation du carousel météo: " + e.getMessage());
        }
    }

    /**
     * Synchronise les filtres entre le haut de la page et le carousel
     */
    private void initFilterSynchronization() {
        // Synchroniser les catégories si les deux contrôles existent
        if (categoryFilterComboBox != null && carouselCategoryFilterComboBox != null) {
            categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    carouselCategoryFilterComboBox.setValue(newVal);
                }
            });
        }
    }

    /**
     * Démarre l'animation automatique du carousel météo
     */
    private void startWeatherCarouselAnimation() {
        // Créer un timer pour changer automatiquement les images toutes les 5 secondes
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    nextWeatherImage();
                });
            }
        }, 5000, 5000); // Délai initial de 5s, puis toutes les 5s
    }

    /**
     * Affiche l'image spécifiée dans le carousel météo
     */
    private void showWeatherImage(int index) {
        // Masquer toutes les images
        for (int i = 0; i < weatherItems.size(); i++) {
            weatherItems.get(i).setVisible(false);
            weatherDots.get(i).getStyleClass().remove("carousel-dot-active");
        }

        // Afficher l'image sélectionnée
        weatherItems.get(index).setVisible(true);
        weatherDots.get(index).getStyleClass().add("carousel-dot-active");

        // Mettre à jour l'index courant
        currentWeatherIndex = index;
    }

    /**
     * Méthodes spécifiques pour chaque image du carousel météo (référencées dans le FXML)
     */
    @FXML
    private void showWeatherImage1() {
        showWeatherImage(0);
    }

    @FXML
    private void showWeatherImage2() {
        showWeatherImage(1);
    }

    @FXML
    private void showWeatherImage3() {
        showWeatherImage(2);
    }

    /**
     * Affiche l'image précédente dans le carousel météo
     */
    @FXML
    private void previousWeatherImage() {
        currentWeatherIndex = (currentWeatherIndex - 1 + weatherItems.size()) % weatherItems.size();
        showWeatherImage(currentWeatherIndex);
    }

    /**
     * Affiche l'image suivante dans le carousel météo
     */
    @FXML
    private void nextWeatherImage() {
        currentWeatherIndex = (currentWeatherIndex + 1) % weatherItems.size();
        showWeatherImage(currentWeatherIndex);
    }

    private void initCategoryFilter() {
        // Récupérer toutes les catégories
        List<CategoryEvent> categories =CategoryService.getAllCategories();

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

        // Configurer les ComboBox
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
        if (categoryFilterComboBox != null) {
            categoryFilterComboBox.setOnAction(e -> handleFilterApply(e));
        }

        // Fin de l'initialisation des filtres

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
        // Récupérer les critères de filtrage
        String searchTerm = searchField.getText().trim().toLowerCase();
        CategoryEvent selectedCategory = categoryFilterComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();
        int minimumScore = 0;

        try {
            minimumScore = Integer.parseInt(scoreFilterField.getText().trim());
        } catch (NumberFormatException e) {
            // Ignorer si le champ est vide ou non numérique
        }

        String selectedStatus = "Tous les états";
        if (statusFilterComboBox != null && statusFilterComboBox.getValue() != null) {
            selectedStatus = statusFilterComboBox.getValue();
        }

        String sortBy = "Date";
        if (sortComboBox != null && sortComboBox.getValue() != null) {
            sortBy = sortComboBox.getValue();
        }

        String order = "Décroissant";
        if (orderComboBox != null && orderComboBox.getValue() != null) {
            order = orderComboBox.getValue();
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
        eventsContainer.getRowConstraints().clear();

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

            // Ajout dans un GridPane à une seule cellule
            eventsContainer.add(emptyMessage, 0, 0, 3, 1); // span sur 3 colonnes

            statusLabel.setText("Aucun résultat trouvé");
        } else {
            // Trier les événements par score (décroissant) si un score minimum est spécifié
            if (minimumScore >= 0) {
                events.sort((e1, e2) -> Integer.compare(getEventScore(e2), getEventScore(e1)));
            }

            // Calculer combien de lignes nous aurons besoin
            int rowCount = (int) Math.ceil(events.size() / 3.0);

            // Ajouter toutes les contraintes de lignes nécessaires
            for (int i = 0; i < rowCount; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.NEVER);
                rowConstraints.setPrefHeight(400); // Hauteur approximative d'une carte
                eventsContainer.getRowConstraints().add(rowConstraints);
            }

            // Ajouter les cartes dans le GridPane
            int rowIndex = 0;
            int colIndex = 0;

            // Ajouter les cartes dans le GridPane
            for (Event e : events) {
                VBox eventCard = createEventCard(e);

                // Ajouter la carte à la prochaine position disponible
                eventsContainer.add(eventCard, colIndex, rowIndex);

                // Passer à la colonne suivante
                colIndex++;

                // Si nous avons rempli une ligne, passer à la suivante
                if (colIndex >= 3) {
                    colIndex = 0;
                    rowIndex++;
                }
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
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPrefWidth(270);
        card.setPrefHeight(430); // Increased height to accommodate weather info

        // Vérifier si l'événement est passé
        boolean isPastEvent = isPastEvent(event);
        if (isPastEvent) {
            card.getStyleClass().add("event-card-past");
        }

        // Image de l'événement
        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(180);
        imageView.getStyleClass().add("event-image");

        // Essayer de charger l'image
        try {
            File imageFile = new File(event.getPhoto());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
            } else {
                // Image par défaut si l'image spécifiée n'existe pas
                Image defaultImage = new Image("/images/default-event.jpg");
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
            try {
                // Image par défaut en cas d'erreur
                Image defaultImage = new Image("/images/default-event.jpg");
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                System.out.println("Erreur lors du chargement de l'image par défaut: " + ex.getMessage());
            }
        }

        // Contenu de l'événement
        VBox content = new VBox(5);
        content.getStyleClass().add("event-content");
        content.setPadding(new Insets(10, 15, 15, 15));

        // Indicateur d'événement passé ou à venir
        HBox statusContainer = new HBox();
        statusContainer.setAlignment(Pos.CENTER_LEFT);
        statusContainer.setPadding(new Insets(0, 0, 5, 0));

        Label statusLabel = new Label();
        if (isPastEvent(event)) {
            statusLabel.setText("PASSÉ");
            statusLabel.getStyleClass().add("event-past-indicator");
        } else {
            statusLabel.setText("À VENIR");
            statusLabel.getStyleClass().add("event-upcoming-indicator");
        }
        statusContainer.getChildren().add(statusLabel);

        // Titre de l'événement
        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);

        // Date de l'événement
        Label dateLabel = new Label("Date: " + event.getDateTime().format(formatter));
        dateLabel.getStyleClass().add("event-date");

        // Catégorie de l'événement - Afficher le type au lieu de l'ID
        String categoryText;
        try {
            CategoryEvent category = categoryService.getById(event.getCategoryevent_id());
            categoryText = (category != null) ? category.getType() : "Catégorie: " + event.getCategoryevent_id();
        } catch (Exception e) {
            categoryText = "Catégorie: " + event.getCategoryevent_id();
        }
        Label categoryLabel = new Label(categoryText);
        categoryLabel.getStyleClass().add("event-category");

        // Score de l'événement
        Label noteLabel = new Label("Score: " + getEventScore(event));
        noteLabel.getStyleClass().add("event-note");

        // Bouton Voir plus
        Button detailsButton = new Button("Voir plus");
        detailsButton.getStyleClass().add("details-button");
        detailsButton.setOnAction(e -> showDetailDialog(event));

        HBox buttonContainer = new HBox();
        buttonContainer.getStyleClass().add("button-container");
        buttonContainer.getChildren().add(detailsButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Ajout des infos météo pour l'événement
        HBox weatherInfoContainer = createWeatherInfoForEvent(event);

        content.getChildren().addAll(statusContainer, titleLabel, dateLabel, categoryLabel, noteLabel, weatherInfoContainer, buttonContainer);
        card.getChildren().addAll(imageView, content);

        return card;
    }

    // Rafraîchir la vue avec les événements mis à jour
    @FXML
    public void refreshTable() {
        System.out.println("Rafraîchissement de la vue...");
        List<Event> events = service.getAll();
        System.out.println("Mise à jour de la vue avec " + events.size() + " événements");

        // Mettre à jour le carousel
        loadCarouselImages();

        // Effacer le conteneur
        eventsContainer.getChildren().clear();
        eventsContainer.getRowConstraints().clear();

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

            // Ajout dans un GridPane à une seule cellule
            eventsContainer.add(emptyMessage, 0, 0, 3, 1); // span sur 3 colonnes

            statusLabel.setText("Aucun événement disponible");
        } else {
            // Ajouter les événements dans un GridPane (3 colonnes)
            int rowIndex = 0;
            int colIndex = 0;

            // Calculer combien de lignes nous aurons besoin
            int rowCount = (int) Math.ceil(events.size() / 3.0);

            // Ajouter toutes les contraintes de lignes nécessaires
            for (int i = 0; i < rowCount; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.NEVER);
                rowConstraints.setPrefHeight(400); // Hauteur approximative d'une carte
                eventsContainer.getRowConstraints().add(rowConstraints);
            }

            // Ajouter les cartes dans le GridPane
            for (Event event : events) {
                VBox eventCard = createEventCard(event);

                // Ajouter la carte à la prochaine position disponible
                eventsContainer.add(eventCard, colIndex, rowIndex);

                // Passer à la colonne suivante
                colIndex++;

                // Si nous avons rempli une ligne, passer à la suivante
                if (colIndex >= 3) {
                    colIndex = 0;
                    rowIndex++;
                }
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
            controllers.back.AjouterEventController controller = loader.getController();
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
            // Créer une fenêtre de dialogue plus grande pour les détails
            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement: " + ev.getTitle());

            // Créer le conteneur principal
            BorderPane mainLayout = new BorderPane();
            mainLayout.setPadding(new Insets(20));
            mainLayout.getStyleClass().add("detail-window");

            // Créer une barre de titre avec le nom de l'événement
            HBox headerBox = new HBox();
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setPadding(new Insets(20, 10, 20, 10)); // Plus d'espace vertical
            headerBox.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 5 5 0 0;");
            headerBox.setMinHeight(70); // Plus haut

            // Titre de l'événement en majuscules et plus grand
            Label headerLabel = new Label(ev.getTitle().toUpperCase());
            headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);");
            headerLabel.setWrapText(true); // Permettre le retour à la ligne pour les titres longs
            headerLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            headerBox.getChildren().add(headerLabel);
            mainLayout.setTop(headerBox);

            // Conteneur central avec 3 parties - Fond légèrement gris pour mieux contraster avec les sections
            HBox contentBox = new HBox(20);
            contentBox.setPadding(new Insets(20));
            contentBox.setAlignment(Pos.TOP_CENTER);
            contentBox.setStyle("-fx-background-color: #f8f9fa;");

            // =================== 1. Image de l'événement ===================
            VBox imageSection = new VBox(10);
            imageSection.setAlignment(Pos.TOP_CENTER);
            imageSection.setPrefWidth(350);

            Label imageTitle = new Label("AFFICHE");
            imageTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-border-color: #34495e; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0;");
            imageTitle.setAlignment(Pos.CENTER);
            imageTitle.setMaxWidth(Double.MAX_VALUE);
            imageSection.getChildren().add(imageTitle);

            // Image
            VBox imageContainer = new VBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setPrefWidth(300);
            imageContainer.setPrefHeight(400);
            imageContainer.setStyle("-fx-background-color: white;");

            // Message de chargement
            Label loadingLabel = new Label("Chargement de l'image...");
            loadingLabel.setStyle("-fx-text-fill: #666;");
            imageContainer.getChildren().add(loadingLabel);

            // Charger l'image de l'événement (ou une image de remplacement)
            if (ev.getPhoto() != null && !ev.getPhoto().isEmpty()) {
                try {
                    File file = new File(ev.getPhoto());
                    if (file.exists()) {
                        // Créer et configurer l'ImageView
                        ImageView eventImage = new ImageView();
                        eventImage.setFitWidth(300);
                        eventImage.setFitHeight(400);
                        eventImage.setPreserveRatio(true);

                        // Charger l'image avec une gestion appropriée
                        Image image = new Image(file.toURI().toString(), 300, 400, true, true);

                        // S'assurer que l'image est chargée correctement
                        if (!image.isError()) {
                            eventImage.setImage(image);
                            imageContainer.getChildren().clear();
                            imageContainer.getChildren().add(eventImage);
                        } else {
                            System.err.println("Erreur de chargement de l'image: " + file.getAbsolutePath());
                            setDefaultEventImage(imageContainer, ev);
                        }
                    } else {
                        System.err.println("Le fichier d'image n'existe pas: " + ev.getPhoto());
                        setDefaultEventImage(imageContainer, ev);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setDefaultEventImage(imageContainer, ev);
                }
            } else {
                setDefaultEventImage(imageContainer, ev);
            }

            // Appliquer des styles à l'image container - Bordure plus épaisse et ombre plus prononcée
            imageContainer.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-padding: 8; -fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 4); -fx-background-radius: 3; -fx-border-radius: 3;");
            imageSection.getChildren().add(imageContainer);

            // =================== 2. Détails de l'événement ===================
            VBox detailsSection = new VBox(15);
            detailsSection.setAlignment(Pos.TOP_LEFT);
            detailsSection.setPrefWidth(350);

            Label detailsTitle = new Label("INFORMATIONS");
            detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-border-color: #34495e; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0;");
            detailsTitle.setMaxWidth(Double.MAX_VALUE);
            detailsSection.getChildren().add(detailsTitle);

            // Status de l'événement (passé ou à venir) - Style amélioré
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(0, 0, 10, 0)); // Ajouter un peu d'espace en dessous

            Label statusLabel = new Label();
            // Créer un style directement au lieu d'utiliser les classes CSS qui pourraient ne pas être chargées correctement
            if (isPastEvent(ev)) {
                statusLabel.setText("PASSÉ");
                statusLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 30;");
            } else {
                statusLabel.setText("À VENIR");
                statusLabel.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-background-radius: 30;");
            }
            statusBox.getChildren().add(statusLabel);
            detailsSection.getChildren().add(statusBox);

            // Créer une boîte pour les détails - Fond blanc avec bordure plus visible
            VBox infoBox = new VBox(15); // Espacement plus grand entre les éléments
            infoBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 3; -fx-border-radius: 3; -fx-border-color: #eaecef; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

            // Date et heure - Style amélioré
            HBox dateBox = new HBox(10);
            Label dateLabel = new Label("Date et heure:");
            dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            // Formater la date avec un style spécial
            Label dateValue;
            if (ev.getDateTime() != null) {
                dateValue = new Label(ev.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                dateValue.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            } else {
                dateValue = new Label("Non spécifiée");
                dateValue.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            }
            dateBox.getChildren().addAll(dateLabel, dateValue);

            // Catégorie - Style amélioré
            HBox categoryBox = new HBox(10);
            Label categoryLabel = new Label("Catégorie:");
            categoryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            Label categoryValue;
            try {
                CategoryEvent category = categoryService.getById(ev.getCategoryevent_id());
                if (category != null) {
                    // Afficher le type de catégorie au lieu de l'ID
                    categoryValue = new Label(category.getType());
                    categoryValue.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 3;");
                } else {
                    categoryValue = new Label("Non spécifiée");
                    categoryValue.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                }
            } catch (Exception e) {
                // Utiliser la valeur par défaut en cas d'erreur
                categoryValue = new Label("Non spécifiée");
                categoryValue.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            }
            categoryBox.getChildren().addAll(categoryLabel, categoryValue);

            // Score - Style amélioré avec étoiles
            HBox scoreBox = new HBox(10);
            Label scoreLabel = new Label("Score:");
            scoreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            int score = getEventScore(ev);
            HBox starBox = new HBox(2);
            starBox.setAlignment(Pos.CENTER_LEFT);

            // Créer les étoiles de score (visuellement plus attrayant qu'un simple nombre)
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < Math.min(score, 10); i++) {
                stars.append("★"); // Étoile pleine
            }
            for (int i = score; i < 10; i++) {
                stars.append("☆"); // Étoile vide
            }

            Label scoreValue = new Label(stars.toString());
            scoreValue.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            scoreBox.getChildren().addAll(scoreLabel, scoreValue);

            // Description - Style amélioré
            VBox descriptionBox = new VBox(8);
            Label descriptionLabel = new Label("Description:");
            descriptionLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            // Créer un TextFlow pour afficher la description avec un style amélioré
            TextFlow descriptionFlow = new TextFlow();
            descriptionFlow.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #eaecef; -fx-border-width: 1; -fx-border-radius: 5;");

            Text descriptionValue;
            if (ev.getDescription() != null && !ev.getDescription().isEmpty()) {
                descriptionValue = new Text(ev.getDescription());
                descriptionValue.setStyle("-fx-fill: #34495e;");
            } else {
                descriptionValue = new Text("Aucune description disponible");
                descriptionValue.setStyle("-fx-fill: #95a5a6; -fx-font-style: italic;");
            }

            // Définir une largeur maximale pour l'enroulement automatique du texte
            descriptionFlow.setPrefWidth(310);
            descriptionFlow.getChildren().add(descriptionValue);

            descriptionBox.getChildren().addAll(descriptionLabel, descriptionFlow);

            // Section météo (remplaçant la section billets) - Style amélioré
            VBox weatherBox = new VBox(10);
            weatherBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #eaecef; -fx-border-width: 1 0 0 0;");

            HBox titleContainer = new HBox();
            titleContainer.setAlignment(Pos.CENTER_LEFT);
            titleContainer.setPadding(new Insets(5, 0, 10, 0));

            Label weatherTitle = new Label("PRÉVISIONS MÉTÉO");
            weatherTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");

            // Container pour l'affichage météo
            VBox weatherContent = new VBox(10);
            weatherContent.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

            // Récupération des données date et localisation de l'événement
            LocalDate eventDate = ev.getDateTime() != null ? ev.getDateTime().toLocalDate() : LocalDate.now();
            String dateStr = ev.getDateTime() != null ? ev.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Date non définie";
            String timeStr = ev.getDateTime() != null ? ev.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";

            // Récupération de la localisation via la catégorie
            String location = "Tunis"; // Valeur par défaut
            int categoryId = ev.getCategoryevent_id();
            if (categoryId > 0) {
                List<CategoryEvent> categories = CategoryService.getAll();
                for (CategoryEvent category : categories) {
                    if (category.getId() == categoryId) {
                        location = category.getLocation();
                        break;
                    }
                }
            }

            // En-tête avec date et lieu
            HBox weatherHeader = new HBox(20);
            weatherHeader.setAlignment(Pos.CENTER_LEFT);

            dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            Label locationLabel = new Label("Lieu: " + location);
            locationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

            weatherHeader.getChildren().addAll(dateLabel, locationLabel);
            weatherContent.getChildren().add(weatherHeader);

            // Séparateur
            Separator separator = new Separator();
            separator.setStyle("-fx-opacity: 0.3;");
            weatherContent.getChildren().add(separator);

            try {
                // Récupération des données météo
                WeatherData weatherData = weatherService.getWeatherForDate(eventDate, location);

                if (weatherData != null) {
                    // Affichage des données météo
                    HBox weatherInfoContainer = new HBox(30);
                    weatherInfoContainer.setAlignment(Pos.CENTER_LEFT);
                    weatherInfoContainer.setPadding(new Insets(15, 0, 15, 0));

                    // Conteneur pour l'icône
                    VBox iconContainer = new VBox();
                    iconContainer.setAlignment(Pos.CENTER);
                    iconContainer.setPrefWidth(100);

                    // Icône météo
                    ImageView weatherIconView = new ImageView();
                    weatherIconView.setFitWidth(80);
                    weatherIconView.setFitHeight(80);

                    // Configuration de l'icône météo en fonction de la condition
                    String weatherIconPath = getWeatherIconPath(weatherData.getCondition());
                    try {
                        Image weatherIcon = new Image(weatherIconPath);
                        weatherIconView.setImage(weatherIcon);
                    } catch (Exception e) {
                        System.out.println("Erreur lors du chargement de l'icône météo: " + e.getMessage());
                    }

                    iconContainer.getChildren().add(weatherIconView);

                    // Conteneur pour les informations textuelles
                    VBox weatherTextInfo = new VBox(10);
                    weatherTextInfo.setPrefWidth(250);

                    // Température avec style accentué
                    Label tempLabel = new Label(String.format("%.1f°C", weatherData.getTemperature()));
                    tempLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

                    // Condition météo
                    Label conditionLabel = new Label(weatherData.getCondition());
                    conditionLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #34495e;");

                    // Humidité
                    Label humidityLabel = new Label(String.format("Humidité: %.0f%%", weatherData.getHumidity()));
                    humidityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

                    weatherTextInfo.getChildren().addAll(tempLabel, conditionLabel, humidityLabel);

                    // Assembler le tout
                    weatherInfoContainer.getChildren().addAll(iconContainer, weatherTextInfo);
                    weatherContent.getChildren().add(weatherInfoContainer);

                    // Ajouter un petit message
                    Label weatherAdvice = new Label(getWeatherAdvice(weatherData.getCondition(), weatherData.getTemperature()));
                    weatherAdvice.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
                    weatherAdvice.setPadding(new Insets(0, 0, 10, 0));
                    weatherContent.getChildren().add(weatherAdvice);
                } else {
                    // Message si les données météo ne sont pas disponibles
                    Label noDataLabel = new Label("Données météo non disponibles pour cette date et ce lieu.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                    noDataLabel.setPadding(new Insets(15, 0, 15, 0));
                    weatherContent.getChildren().add(noDataLabel);
                }
            } catch (Exception e) {
                // Gestion des erreurs
                Label errorLabel = new Label("Impossible de charger les données météo: " + e.getMessage());
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-style: italic;");
                errorLabel.setPadding(new Insets(15, 0, 15, 0));
                weatherContent.getChildren().add(errorLabel);
                e.printStackTrace();
            }

            titleContainer.getChildren().add(weatherTitle);
            weatherBox.getChildren().addAll(titleContainer, weatherContent);

            infoBox.getChildren().addAll(dateBox, categoryBox, scoreBox, descriptionBox, weatherBox);
            detailsSection.getChildren().add(infoBox);

            // =================== 3. QR Code ===================
            VBox qrSection = new VBox(15);
            qrSection.setAlignment(Pos.TOP_CENTER);
            qrSection.setPrefWidth(250);

            Label qrTitle = new Label("CODE QR");
            qrTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-border-color: #34495e; -fx-border-width: 0 0 2 0; -fx-padding: 0 0 5 0;");
            qrTitle.setMaxWidth(Double.MAX_VALUE);
            qrSection.getChildren().add(qrTitle);

            // Créer un conteneur pour le QR code qui sera généré de manière sécurisée
            StackPane qrPane = new StackPane();
            qrPane.setPrefWidth(200);
            qrPane.setPrefHeight(200);
            qrPane.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: white;");

            // Générer le QR code de manière simplifiée pour éviter les erreurs
            try {
                // Générer les données pour le QR Code en texte simple
                String qrContent = "EVENT:" + ev.getId() + "\nTITLE:" + ev.getTitle();

                if (ev.getDateTime() != null) {
                    qrContent += "\nDATE:" + ev.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                }

                qrContent += "\nCATEGORY:" + ev.getCategoryevent_id();

                if (ev.getDescription() != null && !ev.getDescription().isEmpty()) {
                    // Limiter la description à 50 caractères pour simplifier le QR code
                    String shortDesc = ev.getDescription();
                    if (shortDesc.length() > 50) {
                        shortDesc = shortDesc.substring(0, 47) + "...";
                    }
                    qrContent += "\nDESC:" + shortDesc;
                }

                // Configuration simplifiée du QR code avec gestion des erreurs améliorée
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                Map<EncodeHintType, Object> hints = new HashMap<>();
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                hints.put(EncodeHintType.MARGIN, 1);

                // Générer la matrice du QR code
                BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200, hints);

                // Convertir en image JavaFX via BufferedImage
                BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

                // Remplir l'image avec des pixels blancs et noirs selon la matrice
                for (int x = 0; x < 200; x++) {
                    for (int y = 0; y < 200; y++) {
                        int rgb = bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF; // Noir si 1, blanc si 0
                        bufferedImage.setRGB(x, y, rgb);
                    }
                }

                // Convertir BufferedImage en Image JavaFX
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                // Créer l'image JavaFX et la configurer
                ImageView qrImageView = new ImageView(new Image(inputStream));
                qrImageView.setFitWidth(170);
                qrImageView.setFitHeight(170);
                qrImageView.setPreserveRatio(true);

                // Ajouter l'image au conteneur
                qrPane.getChildren().add(qrImageView);

                // Ajouter le bouton pour télécharger le QR - Style amélioré
                Button saveQrButton = new Button("Télécharger le QR Code");
                saveQrButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 4; -fx-cursor: hand;");

                // Version finale de bufferedImage pour l'utiliser dans le lambda
                final BufferedImage finalImage = bufferedImage;

                saveQrButton.setOnAction(e -> {
                    try {
                        File qrFile = new File("qr_" + ev.getId() + "_" + System.currentTimeMillis() + ".png");
                        ImageIO.write(finalImage, "PNG", qrFile);
                        showSuccessAlert("QR Code enregistré avec succès : " + qrFile.getAbsolutePath());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showErrorAlert("Erreur", "Impossible d'enregistrer le QR code", ex.getMessage());
                    }
                });

                // Ajouter des infos sous le QR
                VBox qrInfoBox = new VBox(10);
                qrInfoBox.setAlignment(Pos.CENTER);
                Label qrInfoLabel = new Label("Scannez pour accéder aux détails");
                qrInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                qrInfoBox.getChildren().addAll(qrInfoLabel, saveQrButton);

                // Ajouter le tout à la section QR
                qrSection.getChildren().addAll(qrPane, qrInfoBox);

            } catch (Exception e) {
                e.printStackTrace();

                // En cas d'erreur, afficher un message et un QR code de remplacement
                Label errorLabel = new Label("Impossible de générer le QR code");
                errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

                // Ajouter un rectangle gris comme placeholder
                Rectangle placeholder = new Rectangle(170, 170);
                placeholder.setFill(javafx.scene.paint.Color.LIGHTGRAY);
                qrPane.getChildren().add(placeholder);

                // Ajouter un message d'erreur
                qrSection.getChildren().addAll(qrPane, errorLabel);
            }

            // Ajouter les trois sections au conteneur principal
            contentBox.getChildren().addAll(imageSection, detailsSection, qrSection);
            mainLayout.setCenter(contentBox);

            // Ajouter un pied de page avec une ligne de séparation
            VBox footerContainer = new VBox(10);
            footerContainer.setPadding(new Insets(0, 0, 10, 0));

            // Séparateur entre le contenu et le pied de page
            separator.setStyle("-fx-background-color: #eaecef;");

            // Bouton Fermer plus élégant
            HBox footerBox = new HBox();
            footerBox.setAlignment(Pos.CENTER_RIGHT);
            footerBox.setPadding(new Insets(10, 20, 5, 20));

            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;");
            closeButton.setOnAction(e -> stage.close());

            // Effet hover pour le bouton
            closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;"));
            closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;"));

            footerBox.getChildren().add(closeButton);
            footerContainer.getChildren().addAll(separator, footerBox);
            mainLayout.setBottom(footerContainer);

            // Configurer et afficher la fenêtre
            Scene scene = new Scene(mainLayout, 1000, 700);

            // Appliquer les styles directement sans dépendre de fichiers externes
            // Définir les styles de base directement pour éviter les erreurs de chargement
            mainLayout.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

            try {
                // Essayer de charger la feuille de style, mais capturer toute exception
                String cssPath = getClass().getResource("/vue/styles.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.err.println("Impossible de charger la feuille de style: " + e.getMessage());
                // Continuer sans la feuille de style
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

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
    // Méthode pour formater la date
    private String formatDate(Date date) {
        if (date == null) return "Date non spécifiée";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    /**
     * Crée une image par défaut pour un événement lorsque son image principale n'est pas disponible
     *
     * @param container Le conteneur VBox où placer l'image par défaut
     * @param event     L'événement concerné
     */
    private void setDefaultEventImage(VBox container, Event event) {
        // Nettoyer le conteneur
        container.getChildren().clear();

        // Créer un rectangle comme placeholder
        Rectangle placeholder = new Rectangle(280, 380);
        placeholder.setFill(javafx.scene.paint.Color.web("#f5f7fa"));
        placeholder.setStroke(javafx.scene.paint.Color.web("#eaecef"));
        placeholder.setStrokeWidth(1);

        // Ajouter le titre de l'événement au centre du placeholder
        StackPane placeholderStack = new StackPane();

        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER);

        Label titlePrefix = new Label("ÉVÉNEMENT");
        titlePrefix.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");

        // Utiliser le titre de l'événement ou un message par défaut
        String title = event.getTitle() != null ? event.getTitle() : "Sans titre";
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        titleLabel.setPrefWidth(260);

        String formattedDate = "";
        if (event.getDateTime() != null) {
            formattedDate = event.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Icône d'image non disponible (text uniquement)
        Label iconLabel = new Label("Image non disponible");
        iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");

        textContent.getChildren().addAll(titlePrefix, titleLabel, dateLabel, iconLabel);
        placeholderStack.getChildren().addAll(placeholder, textContent);

        container.getChildren().add(placeholderStack);
    }

    // Méthode pour vérifier si un événement est passé
    private boolean isPastEvent(Event event) {
        if (event.getDateTime() == null) return false;

        LocalDateTime currentDate = LocalDateTime.now();
        return event.getDateTime().isBefore(currentDate);
    }

    /**
     * Crée un composant d'affichage des informations météo pour un événement
     *
     * @param event L'événement pour lequel afficher les informations météo
     * @return Un composant HBox contenant les informations météo
     */
    private HBox createWeatherInfoForEvent(Event event) {
        HBox weatherContainer = new HBox(10);
        weatherContainer.getStyleClass().add("event-weather-container");
        weatherContainer.setAlignment(Pos.CENTER_LEFT);
        weatherContainer.setPadding(new Insets(5, 0, 5, 0));

        // Label pour le titre de la section météo
        Label weatherTitle = new Label("Météo: ");
        weatherTitle.getStyleClass().add("weather-title");

        // ImageView pour l'icône météo
        ImageView weatherIconView = new ImageView();
        weatherIconView.setFitWidth(24);
        weatherIconView.setFitHeight(24);

        // Label pour la condition météo
        Label weatherConditionLabel = new Label();
        weatherConditionLabel.getStyleClass().add("weather-condition");

        // Label pour la température
        Label weatherTempLabel = new Label();
        weatherTempLabel.getStyleClass().add("weather-temp");

        try {
            // Récupération de la date de l'événement
            LocalDate eventDate = event.getDateTime() != null ?
                    event.getDateTime().toLocalDate() : LocalDate.now();

            // Récupération de la localisation de l'événement via sa catégorie
            String location = "Tunis"; // Valeur par défaut

            // Chercher la catégorie pour obtenir la localisation
            int categoryId = event.getCategoryevent_id();
            if (categoryId > 0) { // Vérification avec une valeur valide plutôt que null
                List<CategoryEvent> categories = CategoryService.getAll(); // Utilisation de getAll() au lieu de readAll()
                for (CategoryEvent category : categories) {
                    if (category.getId() == categoryId) { // Comparaison avec l'ID plutôt que le type
                        location = category.getLocation();
                        break;
                    }
                }
            }

            // Récupération des données météo
            WeatherData weatherData = weatherService.getWeatherForDate(eventDate, location);

            if (weatherData != null) {
                // Affichage des données météo
                weatherConditionLabel.setText(weatherData.getCondition());
                weatherTempLabel.setText(String.format("%.1f°C", weatherData.getTemperature()));

                // Configuration de l'icône météo en fonction de la condition
                String weatherIconPath = getWeatherIconPath(weatherData.getCondition());
                try {
                    Image weatherIcon = new Image(weatherIconPath);
                    weatherIconView.setImage(weatherIcon);
                } catch (Exception e) {
                    System.out.println("Erreur lors du chargement de l'icône météo: " + e.getMessage());
                }
            } else {
                // Fallback si les données météo ne sont pas disponibles
                weatherConditionLabel.setText("Données non disponibles");
                weatherTempLabel.setText("");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des données météo: " + e.getMessage());
            weatherConditionLabel.setText("Erreur");
            weatherTempLabel.setText("");
        }

        // Ajout des éléments au conteneur
        weatherContainer.getChildren().addAll(weatherTitle, weatherIconView, weatherConditionLabel, weatherTempLabel);

        return weatherContainer;
    }

    /**
     * Retourne le chemin de l'icône météo en fonction de la condition météo
     * @param condition La condition météo
     * @return Le chemin de l'icône correspondante
     */
    /**
     * Détermine le chemin de l'icône météo en fonction de la condition météo
     *
     * @param condition La condition météo textuelle
     * @return Le chemin de l'icône correspondante
     */
    private String getWeatherIconPath(String condition) {
        String iconPath = "/images/weather/default.png"; // Icône par défaut

        condition = condition.toLowerCase();

        if (condition.contains("soleil") || condition.contains("ensoleill") || condition.contains("clair")) {
            iconPath = "/images/sunny.png";
        } else if (condition.contains("nuage") || condition.contains("couvert")) {
            iconPath = "/images/cloudy.png";
        } else if (condition.contains("pluie") || condition.contains("averse")) {
            iconPath = "/images/rainy.png";
        } else if (condition.contains("orage") || condition.contains("tempête")) {
            iconPath = "/images/stormy.png";
        } else if (condition.contains("neige")) {
            iconPath = "/images/snowy.png";
        } else if (condition.contains("brouillard") || condition.contains("brume")) {
            iconPath = "/images/foggy.png";
        }

        return iconPath;
    }

    /**
     * Génère un conseil adapté aux conditions météo pour l'événement
     *
     * @param condition   La condition météo textuelle
     * @param temperature La température en degrés Celsius
     * @return Un conseil adapté aux conditions météo
     */
    private String getWeatherAdvice(String condition, double temperature) {
        condition = condition.toLowerCase();

        // Conseils basés sur la température
        if (temperature > 30) {
            return "Prévoyez de vous hydrater régulièrement et de vous abriter du soleil pendant cet événement. Tenue légère recommandée.";
        } else if (temperature < 10) {
            return "Habillez-vous chaudement pour cet événement. Prévoyez plusieurs couches de vêtements.";
        }

        // Conseils basés sur les conditions météo
        if (condition.contains("pluie") || condition.contains("averse")) {
            return "N'oubliez pas votre parapluie ou un imperméable pour vous protéger de la pluie.";
        } else if (condition.contains("nuage") || condition.contains("couvert")) {
            return "Temps nuageux prévu, prenez une couche supplémentaire au cas où.";
        } else if (condition.contains("soleil") || condition.contains("ensoleill") || condition.contains("clair")) {
            return "Beau temps prévu ! N'oubliez pas votre protection solaire.";
        } else if (condition.contains("orage")) {
            return "Soyez prudent, des orages sont prévus. Vérifiez la météo avant de vous déplacer.";
        } else if (condition.contains("neige")) {
            return "De la neige est prévue. Prévoyez des chaussures adaptées et des vêtements chauds.";
        } else if (condition.contains("brouillard") || condition.contains("brume")) {
            return "Conditions de visibilité réduite, prévoyez du temps supplémentaire pour vous rendre à l'événement.";
        }

        // Conseil par défaut
        return "Vérifiez la météo avant de vous rendre à l'événement pour préparer votre tenue.";
    }

    /**
     * Réinitialise tous les filtres à leurs valeurs par défaut
     */
    @FXML
    private void clearFilters(ActionEvent event) {
        try {
            // Réinitialiser les valeurs des filtres
            searchField.clear();

            CategoryEvent allCategories = categoryFilterComboBox.getItems().get(0);
            categoryFilterComboBox.setValue(allCategories);

            if (datePicker != null) {
                datePicker.setValue(null);
            }

            if (scoreField != null) {
                scoreField.setText("0");
            }

            if (statusFilterComboBox != null) {
                statusFilterComboBox.setValue("Tous les événements");
            }

            if (sortComboBox != null) {
                sortComboBox.setValue("Date");
            }

            if (orderComboBox != null) {
                orderComboBox.setValue("Décroissant");
            }

            // Appliquer les filtres réinitialisés
            handleFilterApply(new ActionEvent());

            // Mise à jour du statut
            statusLabel.setText("Filtres réinitialisés");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors de la réinitialisation des filtres");
        }
    }

    /**
     * Affiche ou masque les contrôles de filtres avancés
     */
    @FXML
    private void toggleFiltersVisibility(ActionEvent event) {
        if (filtersGrid != null) {
            boolean isVisible = filtersGrid.isVisible();
            filtersGrid.setVisible(!isVisible);
            filtersGrid.setManaged(!isVisible);

            // Mettre à jour le texte et l'icône du bouton
            if (toggleFiltersLabel != null) {
                toggleFiltersLabel.setText(isVisible ? "Afficher les filtres" : "Masquer les filtres");
            }

            if (toggleArrow != null) {
                toggleArrow.setText(isVisible ? "▼" : "▲");
            }
        }
    }
}

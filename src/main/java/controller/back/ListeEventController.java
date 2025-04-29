package controller.back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Event;
import service.Eventservice;
import connect.MyDatabase;
import service.CategoryEventService;
import model.CategoryEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class ListeEventController {

    @FXML
    private TableView<Event> tableView;

    @FXML
    private TableColumn<Event, String> colTitle;

    @FXML
    private TableColumn<Event, String> colDescription;

    @FXML
    private TableColumn<Event, Integer> colCategory;

    @FXML
    private TableColumn<Event, LocalDateTime> colDateTime;

    @FXML
    private TableColumn<Event, String> colPhoto;

    @FXML
    private TableColumn<Event, Void> colActions;

    @FXML
    private FlowPane eventsContainer;

    // Ajout du bouton de notification avec son indicateur
    @FXML
    private Button notificationButton;

    @FXML
    private Label notificationCounter;

    @FXML
    private ImageView notificationIcon;

    // Bouton calendrier
    @FXML
    private Button calendarButton;

    // Champs de recherche
    @FXML
    private TextField searchField;

    // Nouveaux éléments pour le filtrage
    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private DatePicker dateFilter;

    @FXML
    private CheckBox activeEventsOnly;

    @FXML
    private CheckBox historyEventsOnly;

    // Nouveaux éléments pour la pagination
    @FXML
    private Pagination pagination;

    @FXML
    private ComboBox<Integer> rowsPerPageCombo;

    @FXML
    private Label pageInfoLabel;

    private Eventservice service = new Eventservice();

    // Liste des événements originale avant filtrage
    private ObservableList<Event> allEvents = FXCollections.observableArrayList();

    // Nombre d'éléments par page (pour la pagination)
    private int itemsPerPage = 10;

    // Service pour les catégories d'événements
    private CategoryEventService categoryEventService = new CategoryEventService();
    
    // Map pour stocker les types de catégories par ID
    private Map<Integer, String> categoryTypesMap = new HashMap<>();

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur ListeEventController");

        // Test de connexion à la base de données
        testDatabaseConnection();

        // Initialisation des colonnes avec les propriétés exactes de la classe Event
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Configuration spéciale pour l'affichage du type de catégorie au lieu de l'ID
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryevent_id"));
        colCategory.setCellFactory(column -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer categoryId, boolean empty) {
                super.updateItem(categoryId, empty);
                if (empty || categoryId == null) {
                    setText(null);
                } else {
                    setText(getCategoryTypeById(categoryId));
                }
            }
        });
        
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colPhoto.setCellValueFactory(new PropertyValueFactory<>("photo"));

        System.out.println("Colonnes initialisées avec les propriétés: title, description, categoryevent_id, dateTime, photo");

        // Fix for the NullPointerException: check if eventsContainer exists before using it
        if (eventsContainer != null) {
            eventsContainer.setPrefWrapLength(800);
        } else {
            System.out.println("Warning: eventsContainer is null. Check FXML file for missing FlowPane with fx:id='eventsContainer'");
        }

        // Configuration de l'affichage de la date (format datetime)
        configureTableColumns();

        // Initialiser l'icône de notification
        initializeNotificationIcon();
        
        // Créer dynamiquement la checkbox pour l'historique si elle n'existe pas dans le FXML
        if (historyEventsOnly == null) {
            // Créer la check box pour l'historique
            historyEventsOnly = new CheckBox("Afficher l'historique des événements");
            historyEventsOnly.setSelected(false);
            
            // Si la case "Événements actifs uniquement" existe, on place l'historique à côté
            if (activeEventsOnly != null) {
                // Trouver le parent de la case activeEventsOnly
                if (activeEventsOnly.getParent() != null) {
                    // Si le parent est un HBox, on peut simplement ajouter notre nouvelle check box
                    if (activeEventsOnly.getParent() instanceof HBox) {
                        HBox parentBox = (HBox) activeEventsOnly.getParent();
                        parentBox.getChildren().add(historyEventsOnly);
                    } 
                    // Si le parent est un autre type de conteneur, créer un nouveau HBox et remplacer activeEventsOnly
                    else if (activeEventsOnly.getParent() instanceof Pane) {
                        Pane parentPane = (Pane) activeEventsOnly.getParent();
                        
                        // Obtenir la position de activeEventsOnly
                        double x = activeEventsOnly.getLayoutX();
                        double y = activeEventsOnly.getLayoutY();
                        
                        // Créer un HBox pour contenir les deux checkboxes
                        HBox checkboxContainer = new HBox(10); // 10 pixels de spacing
                        checkboxContainer.setLayoutX(x);
                        checkboxContainer.setLayoutY(y);
                        
                        // Retirer activeEventsOnly de son parent actuel
                        parentPane.getChildren().remove(activeEventsOnly);
                        
                        // Ajouter les deux checkboxes au HBox
                        checkboxContainer.getChildren().addAll(activeEventsOnly, historyEventsOnly);
                        
                        // Ajouter le HBox au parent
                        parentPane.getChildren().add(checkboxContainer);
                    }
                }
            }
            
            // Ajouter l'écouteur d'événements à la checkbox d'historique
            historyEventsOnly.selectedProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });
        }

        // Initialiser les éléments de filtrage
        initializeFilterControls();

        // Initialiser les éléments de pagination
        initializePagination();

        // Charger les types de catégories dans notre Map
        loadCategoryTypes();

        // Rafraîchir la table au lancement
        refreshTable();

        // Vérifier les rappels d'événements
        checkUpcomingEvents();

        // Configurer la recherche en temps réel
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 2 || newValue.isEmpty()) {
                    performSearch(newValue);
                }
            });
        }
    }

    private void configureTableColumns() {
        // Configuration de l'affichage de la date (format datetime)
        colDateTime.setCellFactory(column -> new TableCell<Event, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Configuration de l'affichage des photos
        colPhoto.setCellFactory(column -> new TableCell<Event, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);

                if (empty || photoPath == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        File file = new File(photoPath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setText("Photo indisponible");
                        }
                    } catch (Exception e) {
                        setText("Erreur: " + e.getMessage());
                    }
                }
            }
        });

        // Ajouter les boutons d'action (Éditer et Supprimer) à chaque ligne
        colActions.setCellFactory(col -> new TableCell<Event, Void>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnView = new Button("View");
            private final HBox pane = new HBox(5, btnView, btnEdit, btnDelete);

            {
                // Style pour les boutons
                btnView.getStyleClass().add("btn-view");
                btnEdit.getStyleClass().add("btn-edit");
                btnDelete.getStyleClass().add("btn-delete");

                // Styles CSS appliqués directement
                btnView.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-background-radius: 4;");
                btnEdit.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-background-radius: 4;");
                btnDelete.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-background-radius: 4;");

                // Action pour la visualisation
                btnView.setOnAction(e -> {
                    Event ev = getTableView().getItems().get(getIndex());
                    showDetailDialog(ev);
                });

                // Action pour l'édition
                btnEdit.setOnAction(e -> {
                    Event ev = getTableView().getItems().get(getIndex());
                    showEditDialog(ev);
                });

                // Action pour la suppression
                btnDelete.setOnAction(e -> {
                    Event ev = getTableView().getItems().get(getIndex());

                    // Confirmation de suppression
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION,
                            "Êtes-vous sûr de vouloir supprimer cet événement?",
                            ButtonType.YES, ButtonType.NO);
                    confirmDialog.setTitle("Confirmation de suppression");
                    confirmDialog.setHeaderText("Supprimer l'événement: " + ev.getTitle());
                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            service.deleteById(ev.getId());
                            refreshTable();

                            // Afficher une notification de succès
                            showSuccessAlert("Événement supprimé avec succès");
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void initializeFilterControls() {
        // Initialiser le ComboBox des catégories
        if (categoryFilter != null) {
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("Toutes les catégories");

            // Récupérer les types des catégories depuis le service
            try {
                List<CategoryEvent> categories = categoryEventService.getAll();
                for (CategoryEvent category : categories) {
                    String categoryName = category.getType();
                    // Ajouter au cache
                    categoryTypesMap.put(category.getId(), categoryName);
                    // Ajouter à la liste de filtres si pas déjà présent
                    if (!categoryFilter.getItems().contains(categoryName)) {
                    categoryFilter.getItems().add(categoryName);
                    }
                }

                categoryFilter.getSelectionModel().selectFirst();
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des catégories: " + e.getMessage());
                e.printStackTrace();
            }

            // Ajouter un écouteur d'événements
            categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });
        }

        // Initialiser le DatePicker avec la date d'aujourd'hui
        if (dateFilter != null) {
            dateFilter.setValue(null); // Pas de date par défaut
            dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });
        }

        // Initialiser la CheckBox des événements actifs
        if (activeEventsOnly != null) {
            activeEventsOnly.setText("Événements actifs uniquement");
            activeEventsOnly.setSelected(true);
            activeEventsOnly.selectedProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });
        }
        
        // Initialiser la CheckBox pour l'historique des événements
        if (historyEventsOnly != null) {
            historyEventsOnly.setText("Afficher l'historique des événements");
            historyEventsOnly.setSelected(false);
            historyEventsOnly.selectedProperty().addListener((obs, oldVal, newVal) -> {
                applyFilters();
            });
        }
    }

    private void initializePagination() {
        // Initialiser la pagination
        if (pagination != null) {
            pagination.setPageCount(1); // Sera mis à jour dans refreshTable
            pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                updateTableView();
            });
        }
        
        // Initialiser le ComboBox pour le nombre d'éléments par page à 6 fixe
        if (rowsPerPageCombo != null) {
            rowsPerPageCombo.getItems().clear();
            rowsPerPageCombo.getItems().add(6);
            rowsPerPageCombo.setValue(6);
            rowsPerPageCombo.setDisable(true); // Désactiver pour conserver 6 fixe
            itemsPerPage = 6;
        }
    }

    // Nouvelle méthode pour mettre à jour la pagination
    private void updatePagination() {
        if (pagination != null && allEvents != null) {
            int pageCount = (int) Math.ceil((double) allEvents.size() / itemsPerPage);
            pagination.setPageCount(Math.max(1, pageCount));

            if (pagination.getCurrentPageIndex() >= pageCount) {
                pagination.setCurrentPageIndex(0);
            }
        }
    }

    // Nouvelle méthode pour mettre à jour la vue du tableau avec les éléments de la page courante
    private void updateTableView() {
        if (pagination != null && tableView != null && allEvents != null) {
            int startIndex = pagination.getCurrentPageIndex() * itemsPerPage;
            
            // Vérifier si nous avons des filtres actifs
            ObservableList<Event> currentItems = tableView.getItems();
            
            // Si l'objet tableView contient déjà des éléments filtrés, les utiliser
            if (currentItems != null && currentItems != allEvents) {
                System.out.println("Pagination avec des éléments filtrés: " + currentItems.size() + " événements");
                
                // Calculer l'endIndex basé sur les éléments filtrés
                int endIndex = Math.min(startIndex + itemsPerPage, currentItems.size());
                
                if (startIndex >= currentItems.size() && currentItems.size() > 0) {
                    // Si on était sur une page qui n'existe plus avec le nouveau filtre, revenir à la page 0
                    pagination.setCurrentPageIndex(0);
                    startIndex = 0;
                    endIndex = Math.min(itemsPerPage, currentItems.size());
                }
                
                // Assurez-vous que startIndex est dans les limites valides
                if (startIndex < currentItems.size()) {
                    // Créer une nouvelle liste temporaire à afficher
                    ObservableList<Event> pageItems = FXCollections.observableArrayList();
                    for (int i = startIndex; i < endIndex; i++) {
                        pageItems.add(currentItems.get(i));
                    }
                    
                    // Utiliser setItems au lieu de clear()/addAll() pour éviter UnsupportedOperationException
                    tableView.setItems(pageItems);
                    
                    // Mettre à jour le label d'information de page
                    if (pageInfoLabel != null) {
                        pageInfoLabel.setText(String.format("Affichage de %d-%d sur %d événements",
                                startIndex + 1, endIndex, currentItems.size()));
                    }
                } else if (currentItems.isEmpty()) {
                    // Cas où la liste filtrée est vide
                    tableView.setItems(FXCollections.observableArrayList());
                    if (pageInfoLabel != null) {
                        pageInfoLabel.setText("Aucun événement trouvé");
                    }
                }
            } else {
                // Comportement original pour les événements non filtrés
                int endIndex = Math.min(startIndex + itemsPerPage, allEvents.size());
                
                // Utiliser une nouvelle liste temporaire pour éviter les exceptions de modification concurrente
                ObservableList<Event> pageItems = FXCollections.observableArrayList();
                for (int i = startIndex; i < endIndex; i++) {
                    pageItems.add(allEvents.get(i));
                }
                
                // Utiliser setItems au lieu de clear()/addAll()
                tableView.setItems(pageItems);
                
                // Mettre à jour le label d'information de page
                if (pageInfoLabel != null) {
                    if (allEvents.isEmpty()) {
                        pageInfoLabel.setText("Aucun événement trouvé");
                    } else {
                        pageInfoLabel.setText(String.format("Affichage de %d-%d sur %d événements",
                                startIndex + 1, endIndex, allEvents.size()));
                    }
                }
            }
        }
    }

    // Nouvelle méthode pour appliquer les filtres
    @FXML
    public void applyFilters() {
        // Récupérer les valeurs de filtrage
        String selectedCategory = categoryFilter.getValue();
        LocalDate selectedDate = dateFilter.getValue();
        boolean showActiveOnly = activeEventsOnly != null && activeEventsOnly.isSelected();
        boolean showHistoryOnly = historyEventsOnly != null && historyEventsOnly.isSelected();
        
        System.out.println("Applying filters - Category: " + selectedCategory + ", Date: " + selectedDate + 
                         ", Active only: " + showActiveOnly + ", History only: " + showHistoryOnly);
        System.out.println("Total events before filtering: " + allEvents.size());
        
        // Créer une nouvelle liste filtrable
        FilteredList<Event> filteredEvents = new FilteredList<>(allEvents);
        
        filteredEvents.setPredicate(event -> {
            boolean matchesCategory = true;
            boolean matchesDate = true;
            boolean matchesTimeFilter = true;
            
            // Filtre de catégorie
            if (selectedCategory != null && !selectedCategory.equals("Toutes les catégories")) {
                String categoryTypeName = getCategoryTypeById(event.getCategoryevent_id());
                matchesCategory = selectedCategory.equals(categoryTypeName);
            }
            
            // Filtre de date
            if (selectedDate != null) {
                try {
                    LocalDate eventDate = event.getDateTime().toLocalDate();
                    matchesDate = eventDate.equals(selectedDate);
                } catch (Exception e) {
                    System.err.println("Erreur lors du filtrage par date: " + e.getMessage());
                    matchesDate = false;
                }
            }
            
            // Filtre temporel (actifs et/ou historique)
            try {
                boolean isActive = event.getDateTime().isAfter(LocalDateTime.now());
                boolean isHistory = event.getDateTime().isBefore(LocalDateTime.now());
                
                // Logique pour les différentes combinaisons de filtres
                if (showActiveOnly && !showHistoryOnly) {
                    // Uniquement les événements actifs
                    matchesTimeFilter = isActive;
                } else if (!showActiveOnly && showHistoryOnly) {
                    // Uniquement l'historique
                    matchesTimeFilter = isHistory;
                } else if (showActiveOnly && showHistoryOnly) {
                    // Les deux filtres activés - montrer tous les événements
                    matchesTimeFilter = true;
                } else {
                    // Aucun filtre activé - montrer tous les événements
                    matchesTimeFilter = true;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification de la date de l'événement: " + e.getMessage());
                matchesTimeFilter = true; // En cas d'erreur, ne pas filtrer
            }
            
            return matchesCategory && matchesDate && matchesTimeFilter;
        });

        // Compter le nombre d'événements après filtrage
        int filteredCount = 0;
        for (Event e : filteredEvents) {
            filteredCount++;
        }
        System.out.println("Events après filtrage: " + filteredCount);

        // Créer une nouvelle SortedList basée sur la FilteredList
        SortedList<Event> sortedEvents = new SortedList<>(filteredEvents);
        sortedEvents.comparatorProperty().bind(tableView.comparatorProperty());
        
        // Appliquer les filtres en définissant directement les items
        tableView.setItems(sortedEvents);
        
        // S'assurer que les éléments apparaissent même si peu nombreux
        if (filteredCount == 0) {
            System.out.println("Aucun événement trouvé après filtrage.");
        }
        
        // Réinitialiser la pagination à la première page
        if (pagination != null && pagination.getCurrentPageIndex() > 0) {
            pagination.setCurrentPageIndex(0);
        } else {
            // Mettre à jour la pagination si on est déjà sur la première page
            updatePagination();
        }
    }

    // Méthode pour gérer le clic sur le bouton de recherche
    @FXML
    public void handleSearch() {
        if (searchField != null) {
            performSearch(searchField.getText());
        }
    }

    // Méthode pour effectuer la recherche
    private void performSearch(String searchText) {
        System.out.println("Recherche pour: " + searchText);
        
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshTable();  // Si la recherche est vide, afficher tous les événements
            return;
        }
        
        String searchTerm = searchText.toLowerCase().trim();
        
        List<Event> allEventsList = service.getAll();
        ObservableList<Event> searchResults = FXCollections.observableArrayList();

        // Filtrer les événements selon les critères de recherche
        for (Event e : allEventsList) {
            // Récupérer le type de catégorie pour la recherche
            String categoryType = getCategoryTypeById(e.getCategoryevent_id()).toLowerCase();
            
            if ((e.getTitle() != null && e.getTitle().toLowerCase().contains(searchTerm)) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchTerm)) ||
                    categoryType.contains(searchTerm) ||
                    String.valueOf(e.getCategoryevent_id()).equals(searchTerm)) {
                searchResults.add(e);
            }
        }

        System.out.println("Recherche '" + searchTerm + "' - " + searchResults.size() + " résultats trouvés");
        
        // Mettre à jour allEvents avec les résultats de recherche
        allEvents.clear();
        allEvents.addAll(searchResults);
        
        // Réinitialiser la pagination à la première page
        if (pagination != null) {
            pagination.setCurrentPageIndex(0);
        }
        
        // Créer une liste triable pour la table
        SortedList<Event> sortedResults = new SortedList<>(searchResults);
        sortedResults.comparatorProperty().bind(tableView.comparatorProperty());
        
        // Appliquer les résultats à la table
        tableView.setItems(sortedResults);
        
        // Mettre à jour la pagination
        updatePagination();
        
        // Afficher un message si aucun résultat
        if (searchResults.isEmpty()) {
            if (pageInfoLabel != null) {
                pageInfoLabel.setText("Aucun événement trouvé pour: " + searchTerm);
            }
        }
    }

    // Méthode pour gérer le clic sur le bouton de recherche (version ActionEvent)
    @FXML
    private void handleSearch(ActionEvent event) {
        if (searchField != null) {
            performSearch(searchField.getText());
        }
    }

    // Nouvelle méthode pour réinitialiser les filtres

    @FXML
    private void showCalendarView() {
        try {
            // Création d'une nouvelle fenêtre pour le calendrier
            Stage calendarStage = new Stage();
            calendarStage.setTitle("Calendrier des événements");

            // Création des composants UI pour le calendrier
            VBox calendarContainer = new VBox(10);
            calendarContainer.setPadding(new Insets(20));
            calendarContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef); -fx-background-radius: 8;");

            // Ajoutez un titre et une description
            Label titleLabel = new Label("Calendrier des événements");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #343a40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 1, 1);");

            Label infoLabel = new Label("Cliquez sur une date pour voir les événements prévus");
            infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-font-style: italic;");

            // Légende des couleurs
            HBox legendBox = new HBox(15);
            legendBox.setAlignment(Pos.CENTER);
            legendBox.setPadding(new Insets(10, 0, 15, 0));
            
            // Créer des indicateurs de légende
            VBox availableLegend = createLegendItem("Date disponible", "#68d391");
            VBox reservedLegend = createLegendItem("Date réservée", "#f56565");
            VBox todayLegend = createLegendItem("Aujourd'hui", "#4299e1");
            
            legendBox.getChildren().addAll(availableLegend, reservedLegend, todayLegend);

            // Créer des boutons de navigation pour les mois
            HBox navigationBar = new HBox(10);
            navigationBar.setAlignment(Pos.CENTER);
            
            Button prevMonthBtn = new Button("◀ Mois précédent");
            prevMonthBtn.setStyle("-fx-background-color: #4dabf7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 4;");
            
            Label monthYearLabel = new Label();
            monthYearLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #343a40; -fx-min-width: 180px; -fx-alignment: center;");
            
            Button nextMonthBtn = new Button("Mois suivant ▶");
            nextMonthBtn.setStyle("-fx-background-color: #4dabf7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 4;");

            navigationBar.getChildren().addAll(prevMonthBtn, monthYearLabel, nextMonthBtn);

            // Créer un GridPane pour afficher le calendrier
            GridPane calendarGrid = new GridPane();
            calendarGrid.setHgap(10);
            calendarGrid.setVgap(10);
            calendarGrid.setAlignment(Pos.CENTER);
            calendarGrid.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

            // Ajouter les noms des jours de la semaine
            String[] dayNames = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            for (int i = 0; i < 7; i++) {
                Label dayLabel = new Label(dayNames[i]);
                dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #343a40; -fx-padding: 5; -fx-min-width: 100;");
                dayLabel.setAlignment(Pos.CENTER);
                calendarGrid.add(dayLabel, i, 0);
            }

            // Ajouter tous les composants au conteneur principal
            calendarContainer.getChildren().addAll(titleLabel, infoLabel, legendBox, navigationBar, calendarGrid);

            // Créer la scène et l'affecter à la fenêtre
            Scene calendarScene = new Scene(calendarContainer, 800, 600);
            calendarStage.setScene(calendarScene);

            // Récupérer tous les événements
            List<Event> allEvents = service.getAll();
            Map<LocalDate, Boolean> dateStatusMap = new HashMap<>();

            // Remplir la map avec les dates des événements (true = réservé)
            for (Event event : allEvents) {
                dateStatusMap.put(event.getDateTime().toLocalDate(), true);
            }

            // Utiliser AtomicReference pour stocker le YearMonth courant (thread-safe et "effectively final")
            final AtomicReference<YearMonth> currentYearMonthRef = new AtomicReference<>(YearMonth.now());

            // Fonction pour mettre à jour le calendrier
            class CalendarUpdater {
                void updateCalendar() {
                    // Obtenir la valeur YearMonth courant
                    YearMonth currentYearMonth = currentYearMonthRef.get();

                    // Effacer le calendrier actuel (sauf les en-têtes des jours)
                    calendarGrid.getChildren().removeIf(node ->
                            GridPane.getRowIndex(node) != null &&
                                    GridPane.getRowIndex(node) > 0);

                    // Mettre à jour le label du mois et année
                    DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
                    monthYearLabel.setText(currentYearMonth.format(monthYearFormatter));

                    // Déterminer le premier jour du mois
                    LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
                    int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1 pour lundi, 7 pour dimanche

                    // Déterminer le nombre de jours dans le mois
                    int daysInMonth = currentYearMonth.lengthOfMonth();

                    // Remplir le calendrier avec les jours
                    int row = 1;
                    int col = dayOfWeek - 1;

                    LocalDate today = LocalDate.now();

                    for (int day = 1; day <= daysInMonth; day++) {
                        final int finalDay = day;
                        LocalDate date = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), day);
                        
                        // Créer un container pour le jour avec un numéro et potentiellement un indicateur
                        VBox dayContainer = new VBox(5);
                        dayContainer.setAlignment(Pos.CENTER);
                        dayContainer.setMinSize(80, 60);
                        
                        Label dayLabel = new Label(String.valueOf(day));
                        dayLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                        
                        // Ajouter un indicateur d'événements si nécessaire
                        List<Event> eventsForDay = getEventsForDate(allEvents, date);
                        
                        // Style de base pour le bouton de jour
                        String baseStyle = "-fx-background-radius: 5; -fx-padding: 5; -fx-alignment: center;";
                        
                        // Déterminer si la date est réservée ou non, ou si c'est aujourd'hui
                        if (date.equals(today)) {
                            // Aujourd'hui
                            dayContainer.setStyle(baseStyle + "-fx-background-color: #4299e1; -fx-border-color: #2b6cb0; -fx-border-width: 2; -fx-border-radius: 5;");
                            dayLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
                        } else if (dateStatusMap.containsKey(date) && dateStatusMap.get(date)) {
                            // Date réservée (rouge)
                            dayContainer.setStyle(baseStyle + "-fx-background-color: #f56565; -fx-effect: dropshadow(three-pass-box, rgba(255,0,0,0.2), 5, 0, 0, 0);");
                            dayLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
                        } else {
                            // Date disponible (vert)
                            dayContainer.setStyle(baseStyle + "-fx-background-color: #68d391;");
                            dayLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2f855a;");
                        }
                        
                        // Ajouter un indicateur du nombre d'événements
                        if (!eventsForDay.isEmpty()) {
                            Label countLabel = new Label(eventsForDay.size() + " événement" + (eventsForDay.size() > 1 ? "s" : ""));
                            countLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (date.equals(today) ? "white" : "#2d3748") + ";");
                            dayContainer.getChildren().addAll(dayLabel, countLabel);
                        } else {
                            dayContainer.getChildren().add(dayLabel);
                        }

                        // Ajouter une action au clic sur un jour
                        final LocalDate finalDate = date;
                        dayContainer.setOnMouseClicked(e -> {
                            // Récupérer les événements pour cette date
                            List<Event> eventsForClickedDay = getEventsForDate(allEvents, finalDate);

                            if (eventsForClickedDay.isEmpty()) {
                                showInfoAlert("Information", "Aucun événement prévu pour le " +
                                        finalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            } else {
                                // Afficher les événements dans une boîte de dialogue stylisée
                                showEventsDialog(eventsForClickedDay, finalDate);
                            }
                        });

                        calendarGrid.add(dayContainer, col, row);

                        // Passer à la cellule suivante
                        col++;
                        if (col > 6) {
                            col = 0;
                            row++;
                        }
                    }
                }
            }

            CalendarUpdater updater = new CalendarUpdater();

            // Configurer les actions des boutons de navigation
            prevMonthBtn.setOnAction(e -> {
                currentYearMonthRef.set(currentYearMonthRef.get().minusMonths(1));
                updater.updateCalendar();
            });

            nextMonthBtn.setOnAction(e -> {
                currentYearMonthRef.set(currentYearMonthRef.get().plusMonths(1));
                updater.updateCalendar();
            });

            // Afficher le calendrier initial
            updater.updateCalendar();

            // Afficher la fenêtre
            calendarStage.initModality(Modality.APPLICATION_MODAL);
            calendarStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'afficher le calendrier", e.getMessage());
        }
    }

    /**
     * Crée un élément de légende pour le calendrier
     */
    private VBox createLegendItem(String text, String color) {
        VBox item = new VBox(5);
        item.setAlignment(Pos.CENTER);
        
        Rectangle colorRect = new Rectangle(20, 20);
        colorRect.setFill(javafx.scene.paint.Color.web(color));
        colorRect.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
        colorRect.setArcWidth(5);
        colorRect.setArcHeight(5);
        
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        
        item.getChildren().addAll(colorRect, label);
        return item;
    }
    
    /**
     * Affiche une boîte de dialogue stylisée avec les événements d'une date
     */
    private void showEventsDialog(List<Event> events, LocalDate date) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Événements du " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: white;");
        
        Label titleLabel = new Label("Événements du " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #cbd5e0;");
        
        dialogContent.getChildren().addAll(titleLabel, separator);
        
        // Ajouter chaque événement avec un style attrayant
        for (Event event : events) {
            VBox eventBox = new VBox(8);
            eventBox.setPadding(new Insets(10));
            eventBox.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 5;");
            
            Label eventTitle = new Label(event.getTitle());
            eventTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");
            
            Label eventTime = new Label("Heure: " + event.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            eventTime.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
            
            // Récupérer le type de catégorie
            String categoryType = getCategoryTypeById(event.getCategoryevent_id());
            Label eventCategory = new Label("Catégorie: " + categoryType);
            eventCategory.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
            
            Label eventDescription = new Label(event.getDescription());
            eventDescription.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568; -fx-wrap-text: true;");
            eventDescription.setWrapText(true);
            eventDescription.setMaxWidth(400);
            
            eventBox.getChildren().addAll(eventTitle, eventTime, eventCategory, eventDescription);
            
            // Ajouter des boutons d'action
            HBox actionButtons = new HBox(10);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);
            
            Button viewButton = new Button("Voir détails");
            viewButton.setStyle(
                "-fx-background-color: #4299e1;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5 10;" +
                "-fx-background-radius: 4;"
            );
            
            final Event finalEvent = event;
            viewButton.setOnAction(e -> {
                dialogStage.close();
                showDetailDialog(finalEvent);
            });
            
            Button editButton = new Button("Modifier");
            editButton.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
            editButton.setOnAction(e -> {
                dialogStage.close();
                showEditDialog(finalEvent);
            });
            
            actionButtons.getChildren().addAll(viewButton, editButton);
            eventBox.getChildren().add(actionButtons);
            
            dialogContent.getChildren().add(eventBox);
        }
        
        // Ajouter un bouton pour fermer la boîte de dialogue
        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
            "-fx-background-color: #4a5568;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 4;"
        );
        closeButton.setOnAction(e -> dialogStage.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        dialogContent.getChildren().add(buttonBox);
        
        // Ajouter une barre de défilement si nécessaire
        ScrollPane scrollPane = new ScrollPane(dialogContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        Scene scene = new Scene(scrollPane, 450, Math.min(events.size() * 150 + 150, 600));
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    // Initialiser l'icône de notification
    private void initializeNotificationIcon() {
        // Vérifier si le composant existe dans le FXML
        if (notificationButton != null) {
            try {
                // Conteneur pour l'icône et le badge
                StackPane notificationContainer = new StackPane();
                
                // Charger l'icône de notification
                Image notifImage = new Image(getClass().getResourceAsStream("/images/notification.png"));
                if (notificationIcon != null) {
                    notificationIcon.setImage(notifImage);
                    notificationIcon.setFitWidth(24);
                    notificationIcon.setFitHeight(24);
                } else {
                    // Si l'ImageView n'existe pas, créer et configurer dynamiquement
                    notificationIcon = new ImageView(notifImage);
                    notificationIcon.setFitWidth(24);
                    notificationIcon.setFitHeight(24);
                }
                
                // Configurer le style du bouton
                notificationButton.setGraphic(null); // Effacer l'ancien graphique
                notificationButton.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-padding: 5 10;" +
                    "-fx-background-radius: 20;"
                );

                // Configurer le compteur de notifications
                if (notificationCounter != null) {
                    notificationCounter.getStyleClass().add("notification-counter");
                    notificationCounter.setStyle(
                        "-fx-background-color: #e53e3e;" + 
                        "-fx-text-fill: white;" + 
                        "-fx-background-radius: 10;" + 
                        "-fx-padding: 2 6;" + 
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 3, 0, 0, 0);"
                    );
                    notificationCounter.setVisible(false); // Caché par défaut
                    
                    // Positionner le compteur au coin supérieur droit de l'icône
                    StackPane.setAlignment(notificationCounter, Pos.TOP_RIGHT);
                    StackPane.setMargin(notificationCounter, new Insets(-5, -5, 0, 0));
                }
                
                // Assembler le conteneur
                notificationContainer.getChildren().addAll(notificationIcon, notificationCounter);
                notificationButton.setGraphic(notificationContainer);
                
                // Ajouter une transition d'animation sur hover
                notificationButton.setOnMouseEntered(e -> {
                    notificationButton.setStyle(
                        "-fx-background-color: #e2e8f0;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 20;"
                    );
                });
                
                notificationButton.setOnMouseExited(e -> {
                    notificationButton.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 20;"
                    );
                });

                // Ajouter un événement pour afficher les notifications
                notificationButton.setOnAction(event -> showNotifications());

            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation de l'icône de notification: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Le bouton de notification n'a pas été trouvé dans le FXML.");
        }
    }

    // Afficher la fenêtre des notifications
    private void showNotifications() {
        List<Event> upcomingEvents = getUpcomingEvents(2);

        if (upcomingEvents.isEmpty()) {
            showInfoAlert("Notifications", "Aucun événement prévu dans les 2 prochains jours.");
            return;
        }

        // Création d'une fenêtre popup moderne pour afficher les événements à venir
        Stage notificationStage = new Stage();
        notificationStage.setTitle("Événements à venir");
        notificationStage.initModality(Modality.APPLICATION_MODAL);
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white;");
        
        // Titre de la fenêtre
        Label titleLabel = new Label("Rappels d'événements");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #cbd5e0;");
        
        container.getChildren().addAll(titleLabel, separator);
        
        // Parcourir les événements à venir
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Event event : upcomingEvents) {
            // Créer un conteneur pour chaque événement
            VBox eventBox = new VBox(5);
            eventBox.setPadding(new Insets(10));
            eventBox.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 5;");
            
            // Déterminer combien de temps avant l'événement
            long daysUntilEvent = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), event.getDateTime().toLocalDate());
            String timeUntil = daysUntilEvent == 0 ? "Aujourd'hui" : (daysUntilEvent == 1 ? "Demain" : "Dans " + daysUntilEvent + " jours");
            
            // Titre de l'événement avec badge indiquant quand il aura lieu
            HBox titleBox = new HBox(10);
            titleBox.setAlignment(Pos.CENTER_LEFT);
            
            Label eventTitle = new Label(event.getTitle());
            eventTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");
            
            Label timeUntilLabel = new Label(timeUntil);
            timeUntilLabel.setStyle(
                "-fx-background-color: " + (daysUntilEvent == 0 ? "#f56565" : "#4299e1") + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 2 8;" +
                "-fx-background-radius: 12;"
            );
            
            titleBox.getChildren().addAll(eventTitle, timeUntilLabel);
            
            // Détails de l'événement
            Label dateTimeLabel = new Label("📅 " + event.getDateTime().format(formatter));
            dateTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
            
            // Récupérer le type de catégorie
            String categoryType = getCategoryTypeById(event.getCategoryevent_id());
            Label categoryLabel = new Label("🏷️ " + categoryType);
            categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a5568;");
            
            // Description courte
            String shortDesc = event.getDescription();
            if (shortDesc != null && shortDesc.length() > 100) {
                shortDesc = shortDesc.substring(0, 97) + "...";
            }
            Label descriptionLabel = new Label(shortDesc);
            descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; -fx-wrap-text: true;");
            descriptionLabel.setWrapText(true);
            
            // Bouton pour voir les détails
            Button detailsButton = new Button("Voir détails");
            detailsButton.setStyle(
                "-fx-background-color: #4299e1;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5 10;" +
                "-fx-background-radius: 4;"
            );
            
            final Event finalEvent = event;
            detailsButton.setOnAction(e -> {
                notificationStage.close();
                showDetailDialog(finalEvent);
            });
            
            HBox buttonBox = new HBox(detailsButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            eventBox.getChildren().addAll(titleBox, dateTimeLabel, categoryLabel, descriptionLabel, buttonBox);
            container.getChildren().add(eventBox);
        }
        
        // Bouton pour fermer les notifications
        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
            "-fx-background-color: #4a5568;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 20;" +
            "-fx-background-radius: 4;"
        );
        closeButton.setOnAction(e -> notificationStage.close());
        
        HBox closeButtonBox = new HBox(closeButton);
        closeButtonBox.setAlignment(Pos.CENTER);
        closeButtonBox.setPadding(new Insets(10, 0, 0, 0));
        
        container.getChildren().add(closeButtonBox);
        
        // Créer un ScrollPane si nécessaire
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        Scene scene = new Scene(scrollPane, 450, Math.min(upcomingEvents.size() * 150 + 150, 600));
        notificationStage.setScene(scene);
        notificationStage.showAndWait();
    }

    // Méthodes existantes
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
                    }
                }
            } else {
                System.err.println("❌ La connexion à la base de données a échoué!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du test de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshTable() {
        System.out.println("Rafraîchissement de la table...");
        List<Event> events = service.getAll();
        System.out.println("Récupération de " + events.size() + " événements depuis la base de données");

        // Vérifier si nous avons récupéré des événements
        if (events.isEmpty()) {
            System.out.println("⚠️ Aucun événement récupéré de la base de données");
        } else {
            System.out.println("Liste des événements chargés:");
            for (Event e : events) {
                System.out.println("ID: " + e.getId() + ", Titre: " + e.getTitle() +
                        ", Description: " + e.getDescription() +
                        ", Catégorie: " + e.getCategoryevent_id() + " (" + getCategoryTypeById(e.getCategoryevent_id()) + ")" +
                        ", Date: " + e.getDateTime() +
                        ", Photo: " + e.getPhoto());
            }
        }
        
        // Convertir en ObservableList
        allEvents.clear();
        allEvents.addAll(events);
        
        // Réinitialiser les filtres si nécessaire
        if (categoryFilter != null) {
            categoryFilter.getSelectionModel().select("Toutes les catégories");
        }
        if (dateFilter != null) {
            dateFilter.setValue(null);
        }

        // Appliquer les filtres avec la nouvelle liste
        if (categoryFilter != null && dateFilter != null && activeEventsOnly != null) {
            System.out.println("Application des filtres après rechargement...");
            applyFilters();
        } else {
            // Mettre à jour directement la vue si les filtres ne sont pas disponibles
            System.out.println("Mise à jour directe de la TableView...");
            // Réinitialiser la pagination
            if (pagination != null) {
                pagination.setCurrentPageIndex(0);
            }
            tableView.setItems(allEvents);
            updateTableView();
        }

        // Vérifier les rappels après chaque rafraîchissement
        checkUpcomingEvents();
    }

    // Le reste de vos méthodes (openAddForm, showEditDialog, etc.)
    @FXML
    private void openAddForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/AjouterEvent.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // S'assurer que la table est rafraîchie lorsque la fenêtre d'ajout est fermée
            stage.setOnHidden(e -> {
                System.out.println("Fenêtre d'ajout fermée, rafraîchissement de la table...");
            refreshTable();
                
                // Après le rafraîchissement, afficher le nombre d'événements pour débogage
                System.out.println("Table rafraîchie, nombre d'événements après: " + tableView.getItems().size());
                
                // Mettre à jour la pagination après l'ajout
                updatePagination();
                updateTableView();
            });
            
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout", e.getMessage());
        }
    }

    private void showEditDialog(Event ev) {
        try {
            // Vérifier si l'événement est valide
            if (ev == null) {
                showErrorAlert("Erreur", "Événement invalide", "Impossible de modifier cet événement.");
                return;
            }
            
            System.out.println("Ouverture du formulaire d'édition pour l'événement ID: " + ev.getId());
            
            // Utiliser le chemin correct pour EditEventView.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/back/EditEventView.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et initialiser avec l'événement à modifier
            controller.EditEventController controller = loader.getController();
            controller.initData(ev);

            Stage stage = new Stage();
            stage.setTitle("Modifier un événement");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Ajouter un listener pour détecter la fermeture
            stage.setOnHidden(e -> {
                System.out.println("Formulaire d'édition fermé, rafraîchissement de la table...");
                refreshTable();
            });
            
            stage.showAndWait();

            // Après la fermeture de la fenêtre, rafraîchir la liste
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur détaillée: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire d'édition", 
                    "Détails: " + e.getMessage() + "\nVérifiez le chemin du fichier FXML et le contrôleur.");
        }
    }

    private void showDetailDialog(Event ev) {
        // Créer simplement une boîte de dialogue avec les détails
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails de l'événement");
            alert.setHeaderText(ev.getTitle());

            String details = "ID: " + ev.getId() + "\n" +
                    "Titre: " + ev.getTitle() + "\n" +
                    "Description: " + (ev.getDescription() != null ? ev.getDescription() : "Aucune") + "\n" +
                    "Catégorie: " + ev.getCategoryevent_id() + "\n" +
                    "Date et heure: " + ev.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                    "Photo: " + (ev.getPhoto() != null ? ev.getPhoto() : "Aucune");

            alert.setContentText(details);
            alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleExport(ActionEvent event) {
        List<Event> events = tableView.getItems();

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

    private String csvEscape(String value) {
        if (value == null) return "";

        // Échapper les guillemets en les doublant et entourer de guillemets si nécessaire
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Méthode pour récupérer le type de catégorie à partir de son ID
    private String getCategoryTypeById(int categoryId) {
        // Essayer d'abord de récupérer depuis le cache
        if (categoryTypesMap.containsKey(categoryId)) {
            return categoryTypesMap.get(categoryId);
        } 
        
        // Si pas en cache, charger depuis la base de données
        CategoryEvent category = categoryEventService.getById(categoryId);
        if (category != null) {
            String categoryType = category.getType();
            // Stocker dans le cache pour les prochaines requêtes
            categoryTypesMap.put(categoryId, categoryType);
            return categoryType;
        }
        
        // Valeur par défaut si non trouvé
        return "Catégorie " + categoryId;
    }
    
    // Méthode pour charger tous les types de catégories
    private void loadCategoryTypes() {
        try {
            // Utiliser le service de catégories pour obtenir toutes les catégories
            List<CategoryEvent> categories = categoryEventService.getAll();
            
            // Remplir notre map avec les données
            for (CategoryEvent category : categories) {
                categoryTypesMap.put(category.getId(), category.getType());
            }
            
            System.out.println("Types de catégories chargés: " + categoryTypesMap.size());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des types de catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer les événements pour une date spécifique
    private List<Event> getEventsForDate(List<Event> allEvents, LocalDate date) {
        List<Event> eventsForDate = new ArrayList<>();

        for (Event event : allEvents) {
            LocalDate eventDate = event.getDateTime().toLocalDate();
            if (eventDate.equals(date)) {
                eventsForDate.add(event);
            }
        }

        return eventsForDate;
    }

    // Vérifier les événements à venir dans les 2 prochains jours
    private void checkUpcomingEvents() {
        List<Event> upcomingEvents = getUpcomingEvents(2); // 2 jours

        if (!upcomingEvents.isEmpty()) {
            // Si nous avons des événements à venir, afficher le compteur
            if (notificationCounter != null) {
                notificationCounter.setText(String.valueOf(upcomingEvents.size()));
                notificationCounter.setVisible(true);
            }

            // Rendre le bouton de notification visible s'il existe
            if (notificationButton != null) {
                notificationButton.setStyle("-fx-background-color: #ff6b6b;"); // Couleur de fond rouge clair
            }
        } else {
            // Pas d'événements à venir, cacher le compteur
            if (notificationCounter != null) {
                notificationCounter.setVisible(false);
            }

            // Remettre le style normal
            if (notificationButton != null) {
                notificationButton.setStyle(""); // Style par défaut
            }
        }
    }

    // Récupérer les événements à venir dans les N prochains jours
    private List<Event> getUpcomingEvents(int days) {
        List<Event> upcomingEvents = new ArrayList<>();
        List<Event> allEvents = service.getAll();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusDays(days);

        for (Event event : allEvents) {
            LocalDateTime eventDate = event.getDateTime();
            if (eventDate.isAfter(now) && eventDate.isBefore(twoDaysLater)) {
                upcomingEvents.add(event);
            }
        }

        return upcomingEvents;
    }
}
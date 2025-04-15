package controller;

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
import model.Event;
import service.Eventservice;
import connect.MyDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListeEventController {

    @FXML
    private FlowPane eventsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    private Eventservice service = new Eventservice();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur ListeEventController");

        // Test de connexion à la base de données
        testDatabaseConnection();

        // Rafraîchir la vue au lancement
        refreshTable();
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

    // Création d'une carte pour un événement
    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");

        // Titre de l'événement
        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("event-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Date et heure
        Label dateLabel = new Label(event.getDateTime().format(formatter));
        dateLabel.getStyleClass().add("event-date");

        // Catégorie
        Label categoryLabel = new Label("Catégorie " + event.getCategoryevent_id());
        categoryLabel.getStyleClass().add("event-category");

        // Image (si disponible)
        HBox imageContainer = new HBox();
        imageContainer.getStyleClass().add("event-image-container");
        imageContainer.setMaxWidth(Double.MAX_VALUE);
        imageContainer.setPrefHeight(120);

        if (event.getPhoto() != null && !event.getPhoto().isEmpty()) {
            try {
                File file = new File(event.getPhoto());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(), 150, 100, true, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(100);
                    imageContainer.getChildren().add(imageView);
                    imageContainer.setAlignment(Pos.CENTER);
                } else {
                    Text noImageText = new Text("Image non disponible");
                    imageContainer.getChildren().add(noImageText);
                    imageContainer.setAlignment(Pos.CENTER);
                }
            } catch (Exception e) {
                Text errorText = new Text("Erreur de chargement");
                imageContainer.getChildren().add(errorText);
                imageContainer.setAlignment(Pos.CENTER);
            }
        } else {
            Text noImageText = new Text("Aucune image");
            imageContainer.getChildren().add(noImageText);
            imageContainer.setAlignment(Pos.CENTER);
        }

        // Description (limitée avec ellipsis)
        String descriptionText = event.getDescription();
        if (descriptionText != null && !descriptionText.isEmpty()) {
            if (descriptionText.length() > 100) {
                descriptionText = descriptionText.substring(0, 97) + "...";
            }
        } else {
            descriptionText = "Aucune description disponible";
        }

        Label descriptionLabel = new Label(descriptionText);
        descriptionLabel.getStyleClass().add("event-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);
        descriptionLabel.setMaxHeight(60);

        // Boutons d'action
        HBox actionButtons = new HBox(5);
        actionButtons.getStyleClass().add("action-buttons");

        Button btnView = new Button("Voir");
        btnView.getStyleClass().add("btn-view");

        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("btn-edit");

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().add("btn-delete");

        actionButtons.getChildren().addAll(btnView, btnEdit, btnDelete);

        // Ajouter les actions aux boutons
        btnView.setOnAction(e -> showDetailDialog(event));
        btnEdit.setOnAction(e -> showEditDialog(event));
        btnDelete.setOnAction(e -> {
            // Confirmation de suppression
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
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        titleBox.getChildren().addAll(titleLabel, dateLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, categoryLabel);

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

            emptyMessage.getChildren().addAll(emptyLabel, new Region() {{ setPrefHeight(20); }}, addButton);
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

    // Ouvrir le formulaire d'ajout d'événement
    @FXML
    private void openAddForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/Ajouterevent.fxml"));
            Parent root = loader.load();

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

            emptyMessage.getChildren().addAll(emptyLabel, new Region() {{ setPrefHeight(20); }}, clearButton);
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
}
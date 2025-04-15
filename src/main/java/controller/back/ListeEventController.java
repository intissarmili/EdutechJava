package controller.back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableView<Event> tableView;

    @FXML
    private TableColumn<Event, String> colTitle;

    @FXML
    private TableColumn<Event, String> colDescription;  // Nouvelle colonne pour la description

    @FXML
    private TableColumn<Event, Integer> colCategory;

    @FXML
    private TableColumn<Event, LocalDateTime> colDateTime;

    @FXML
    private TableColumn<Event, String> colPhoto;  // Nouvelle colonne pour la photo

    @FXML
    private TableColumn<Event, Void> colActions;

    private Eventservice service = new Eventservice();

    @FXML
    public void initialize() {
        System.out.println("Initialisation du contrôleur ListeEventController");

        // Test de connexion à la base de données
        testDatabaseConnection();

        // Initialisation des colonnes avec les propriétés exactes de la classe Event
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));  // Nouvelle colonne
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryevent_id"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colPhoto.setCellValueFactory(new PropertyValueFactory<>("photo"));  // Nouvelle colonne

        System.out.println("Colonnes initialisées avec les propriétés: title, description, categoryevent_id, dateTime, photo");

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
            private final Button btnView = new Button("View"); // Nouveau bouton pour voir les détails
            private final HBox pane = new HBox(5, btnView, btnEdit, btnDelete);

            {
                // Style pour les boutons
                btnView.getStyleClass().add("btn-view");
                btnEdit.getStyleClass().add("btn-edit");
                btnDelete.getStyleClass().add("btn-delete");

                // Action pour la visualisation
                btnView.setOnAction(e -> {
                    Event ev = getTableView().getItems().get(getIndex());
                    showDetailDialog(ev);  // Afficher les détails de l'événement
                });

                // Action pour l'édition
                btnEdit.setOnAction(e -> {
                    Event ev = getTableView().getItems().get(getIndex());
                    showEditDialog(ev);  // Afficher le formulaire d'édition
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
                            service.deleteById(ev.getId());  // Supprimer l'événement
                            refreshTable();  // Rafraîchir la table

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

        // Rafraîchir la table au lancement
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

    // Rafraîchir la table avec les événements mis à jour
    @FXML
    public void refreshTable() {
        System.out.println("Rafraîchissement de la table...");
        List<Event> events = service.getAll();
        System.out.println("Mise à jour du tableau avec " + events.size() + " événements");

        // Effacer et recharger tous les éléments
        tableView.getItems().clear();
        tableView.getItems().addAll(events);

        // Si aucun événement n'est affiché, afficher un message de débogage
        if (events.isEmpty()) {
            System.out.println("⚠️ Aucun événement récupéré de la base de données");
        } else {
            System.out.println("Liste des événements chargés:");
            for (Event e : events) {
                System.out.println("ID: " + e.getId() + ", Titre: " + e.getTitle() +
                        ", Description: " + e.getDescription() +
                        ", Catégorie: " + e.getCategoryevent_id() +
                        ", Date: " + e.getDateTime() +
                        ", Photo: " + e.getPhoto());
            }
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
            controller. initData(ev);

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/EventDetailView.fxml"));
            Parent root = loader.load();

            // Si vous avez un contrôleur pour la vue détaillée, récupérez-le et initialisez-le
            // EventDetailController controller = loader.getController();
            // controller.initData(ev);

            // Sinon, créez simplement une boîte de dialogue avec les détails
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
        } catch (IOException e) {
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

    // Bouton de recherche
    @FXML
    private TextField searchField;

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

        // Mettre à jour la table avec les résultats filtrés
        tableView.getItems().clear();
        tableView.getItems().addAll(filteredEvents);

        System.out.println("Recherche '" + searchTerm + "' - " + filteredEvents.size() + " résultats trouvés");
    }

    // Exporter la liste des événements
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
package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Feed;
import service.FeedService;
import service.CommentaireService;
import service.FeedHistoryService;
import models.FeedHistory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.collections.FXCollections;

public class ListFeedController {
    @FXML
    private TableView<Feed> tableFeeds;
    
    @FXML
    private TableColumn<Feed, String> colFeedText;
    
    @FXML
    private TableColumn<Feed, String> colLastModified;
    
    @FXML
    private TableColumn<Feed, Void> colActions;
    
    private FeedService feedService;
    private CommentaireService commentaireService = new CommentaireService();
    private FeedHistoryService feedHistoryService = new FeedHistoryService();

    @FXML
    public void initialize() {
        feedService = new FeedService();
        setupTableColumns();
        loadFeeds();
    }
    
    private void setupTableColumns() {
        // Configuration de la colonne de texte
        colFeedText.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPublication()));
        
        // Configuration de la colonne de date
        colLastModified.setCellValueFactory(cellData -> {
            LocalDateTime lastModified = cellData.getValue().getLastModified();
            String formattedDate = lastModified != null ? 
                lastModified.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        
        // Configuration de la colonne d'actions
        colActions.setCellFactory(column -> new TableCell<Feed, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(event -> {
                    Feed feed = getTableView().getItems().get(getIndex());
                    editFeed(feed);
                });
                
                deleteButton.setOnAction(event -> {
                    Feed feed = getTableView().getItems().get(getIndex());
                    deleteFeed(feed);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadFeeds() {
        try {
            List<Feed> feeds = feedService.getAllFeeds();
            tableFeeds.getItems().setAll(feeds);
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Impossible de charger les publications : " + e.getMessage());
        }
    }
    
    private void editFeed(Feed feed) {
        TextInputDialog dialog = new TextInputDialog(feed.getPublication());
        dialog.setTitle("Modifier la publication");
        dialog.setHeaderText("Modifiez votre publication :");
        dialog.setContentText("Contenu :");

        dialog.showAndWait().ifPresent(content -> {
            if (!content.trim().isEmpty()) {
                String oldContent = feed.getPublication();
                feed.setPublication(content);
                feed.setLastModified(LocalDateTime.now());
                try {
                    feedService.updateFeed(feed);
                    loadFeeds();
                    showInfoAlert("Succès", "Publication modifiée avec succès !");

                    FeedHistory history = new FeedHistory(
                        feed.getId(),
                        oldContent,
                        content,
                        "UPDATE"
                    );
                    feedHistoryService.addHistory(history);
                } catch (SQLException e) {
                    showErrorAlert("Erreur", "Impossible de modifier la publication : " + e.getMessage());
                }
            }
        });
    }
    
    private void deleteFeed(Feed feed) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Feed_Item.fxml"));
            Parent root = loader.load();
            FeedItemController controller = loader.getController();
            controller.setFeed(feed);
            controller.setRefreshCallback(this::loadFeeds); // Optional: refresh list after delete

            // Call the delete method
            controller.handleDelete();

            // Optionally, show the FeedItem view in a new window or dialog if you want
            // Stage stage = new Stage();
            // stage.setScene(new Scene(root));
            // stage.show();

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de supprimer la publication : " + e.getMessage());
        }
    }

    @FXML
    public void goToCreateFeed() {
        try {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nouvelle publication");
            dialog.setHeaderText("Écrivez votre publication :");
            dialog.setContentText("Contenu :");

            dialog.showAndWait().ifPresent(content -> {
                if (!content.trim().isEmpty()) {
                    Feed newFeed = new Feed();
                    newFeed.setPublication(content);
                    newFeed.setLastModified(LocalDateTime.now());
                    try {
                        feedService.createFeed(newFeed);
                        loadFeeds();
                        showInfoAlert("Succès", "Publication créée avec succès !");
                    } catch (SQLException e) {
                        showErrorAlert("Erreur", "Impossible de créer la publication : " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            showErrorAlert("Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }
    
    @FXML
    public void showAllFeedHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FeedHistory.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Historique des publications");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir l'historique : " + e.getMessage());
        }
    }
    
    @FXML
    public void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FeedStatistics.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Statistiques des publications");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir les statistiques : " + e.getMessage());
        }
    }
    
    @FXML
    public void showTranslationTest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TranslationTest.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Test de traduction");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le test de traduction : " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setFeedIdNullInHistory(int feedId) {
        try {
            java.sql.Connection conn = utils.MaConnexion.getInstance().getConnection();
            String sql = "UPDATE feed_history SET feed_id = NULL WHERE feed_id = ?";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, feedId);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            // Optionally log or show error
        }
    }
}

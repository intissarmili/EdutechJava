package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Commentaire;
import models.Feed;
import models.TranslatedComment;
import service.CommentaireService;
import service.FeedService;
import service.TranslationService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FeedItemController {

    @FXML private Label feedContent;
    @FXML private VBox commentsContainer;
    @FXML private TextField newCommentField;
    @FXML private VBox feedItemBox;
    @FXML private Label commentLengthLabel;
    @FXML private Label lastModifiedLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Feed feed;
    private FeedService feedService = new FeedService();
    private CommentaireService commentaireService = new CommentaireService();
    private TranslationService translationService = new TranslationService();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Runnable refreshCallback;
    
    // Map to keep track of comment translations
    private Map<Integer, TranslatedComment> translatedComments = new HashMap<>();

    @FXML
    public void initialize() {
        feedService = new FeedService();
    }

    @FXML
    private void checkCommentLength() {
        int length = newCommentField.getText().length();
        commentLengthLabel.setText(length + "/300");

        if (length > 300) {
            commentLengthLabel.setStyle("-fx-text-fill: red;");
        } else {
            commentLengthLabel.setStyle("-fx-text-fill: grey;");
        }
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
        feedContent.setText(feed.getPublication());
        
        // Afficher la date de dernière modification
        if (feed.getLastModified() != null) {
            lastModifiedLabel.setText(feed.getLastModified().format(dateFormatter));
        } else {
            lastModifiedLabel.setText("Non disponible");
        }
        
        loadComments();
        addVoirCommentairesButton(); // Ajout ici
    }

    public void setFeedService(FeedService feedService) {
        this.feedService = feedService;
    }

    public void setCommentaireService(CommentaireService commentaireService) {
        this.commentaireService = commentaireService;
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        translatedComments.clear(); // Clear the translations map
        
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesByFeedId(feed.getId());
            for (Commentaire comment : commentaires) {
                // Create a TranslatedComment object for each comment
                TranslatedComment translatedComment = createTranslatedComment(comment);
                translatedComments.put(comment.getId(), translatedComment);
                createCommentBox(translatedComment);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires");
        }
    }
    
    private TranslatedComment createTranslatedComment(Commentaire comment) {
        TranslatedComment translatedComment = new TranslatedComment(comment);
        
        // Add translations for all available languages
        for (String language : translationService.getAvailableLanguages()) {
            if (!language.equals(TranslationService.FRENCH)) { // Skip the original language
                String translatedText = translationService.translate(comment.getContenu(), language);
                translatedComment.addTranslation(language, translatedText);
            }
        }
        
        return translatedComment;
    }

    private void createCommentBox(TranslatedComment translatedComment) {
        Commentaire comment = translatedComment.getOriginalComment();
        
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-padding: 8; -fx-background-color: #f5f5f5; -fx-border-radius: 3;");
        
        // Language selector
        HBox languageSelector = new HBox(5);
        languageSelector.setStyle("-fx-padding: 0 0 5 0;");
        
        Label languageLabel = new Label("Langue: ");
        languageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
        
        ComboBox<String> languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll(translationService.getAvailableLanguages());
        languageComboBox.setValue(TranslationService.FRENCH); // Default to French
        
        languageSelector.getChildren().addAll(languageLabel, languageComboBox);
        
        // Comment content
        Label commentLabel = new Label(comment.getContenu());
        commentLabel.setWrapText(true);
        
        // Actions
        HBox actionsBox = new HBox(5);
        Button btnLike = new Button("👍 " + comment.getUpVotes());
        Button btnDislike = new Button("👎 " + comment.getDownVotes());
        Button btnEdit = new Button("✏️");
        Button btnDelete = new Button("🗑️");

        btnEdit.setOnAction(e -> handleEditComment(comment));
        btnDelete.setOnAction(e -> handleDeleteComment(comment));
        btnLike.setOnAction(e -> handleLikeComment(comment));
        btnDislike.setOnAction(e -> handleDislikeComment(comment));
        
        // Handle language change
        languageComboBox.setOnAction(e -> {
            String selectedLanguage = languageComboBox.getValue();
            translatedComment.setCurrentLanguage(selectedLanguage);
            commentLabel.setText(translatedComment.getCurrentText());
        });

        actionsBox.getChildren().addAll(btnLike, btnDislike, btnEdit, btnDelete);
        
        // Combine all elements
        HBox contentBox = new HBox(10);
        contentBox.getChildren().addAll(commentLabel, actionsBox);
        
        commentBox.getChildren().addAll(languageSelector, contentBox);
        commentsContainer.getChildren().add(commentBox);
    }

    private void addVoirCommentairesButton() {
        Button btnVoirCommentaires = new Button("🗂️ Voir tous les commentaires");
        btnVoirCommentaires.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;");
        btnVoirCommentaires.setOnAction(e -> openCommentairesScene(feed));
        commentsContainer.getChildren().add(btnVoirCommentaires); // Ajout à la fin
    }

    private void openCommentairesScene(Feed feed) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListCommentaires.fxml"));
            Parent root = loader.load();
            ListCommentairesController controller = loader.getController();
            controller.setFeed(feed);

            Scene scene = new Scene(root);
            Stage stage = (Stage) feedContent.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture: " + e.getMessage());
            e.printStackTrace(); // Pour faciliter le débug
        } catch (Exception e) {
            showError("Erreur inattendue: " + e.getMessage());
            e.printStackTrace(); // Pour faciliter le débug
        }
    }

    @FXML
    private void handleEdit() {
        TextInputDialog dialog = new TextInputDialog(feed.getPublication());
        dialog.setTitle("Modifier la publication");
        dialog.setHeaderText("Modifiez votre publication :");
        dialog.setContentText("Contenu :");

        dialog.showAndWait().ifPresent(content -> {
            if (!content.trim().isEmpty()) {
                feed.setPublication(content);
                feed.setLastModified(java.time.LocalDateTime.now());
                try {
                    feedService.updateFeed(feed);
                    // Notify parent to refresh the list if needed
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("Publication modifiée avec succès !");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de modifier la publication : " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

   @FXML
public void handleDelete() {
    try {
        // 1. Delete all comments for this feed
        commentaireService.getCommentairesByFeedId(feed.getId())
            .forEach(comment -> {
                try {
                    commentaireService.deleteCommentaire(comment.getId());
                } catch (Exception ex) {
                    // Optionally log or show error for individual comment deletion
                }
            });

        // 2. Set feed_id to NULL in feed_history for this feed
        setFeedIdNullInHistory(feed.getId());

        // 3. Now delete the feed
        feedService.deleteFeed(feed.getId());

        // Notify parent to refresh the list
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    } catch (Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Failed to delete feed: " + e.getMessage());
        alert.showAndWait();
    }
}

private void setFeedIdNullInHistory(int feedId) {
    try {
        // You need a connection to your DB here
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

    @FXML
    private void handleAddComment() {
        String contenu = newCommentField.getText().trim();

        if (contenu.isEmpty()) {
            showError("Veuillez saisir un commentaire");
            return;
        }

        if (contenu.length() > 300) {
            showError("Le commentaire ne doit pas dépasser 300 caractères");
            return;
        }

        if (isSpam(contenu)) {
            showError("Le commentaire semble être du spam");
            return;
        }

        Commentaire newComment = new Commentaire();
        newComment.setContenu(contenu);
        newComment.setFeedId(feed.getId());

        try {
            commentaireService.addCommentaire(newComment);
            newCommentField.clear();
            refreshCallback.run();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private boolean isSpam(String text) {
        if (text.split(" ").length > 30) return true;
        return text.matches(".*\\b(?:viagra|casino|porn|free money)\\b.*");
    }

    private void handleEditComment(Commentaire comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContenu());
        dialog.setTitle("Modifier Commentaire");
        dialog.setHeaderText("Modifier le commentaire :");
        dialog.showAndWait().ifPresent(newText -> {
            if (newText.length() > 300) {
                showError("Le commentaire ne doit pas dépasser 300 caractères");
                return;
            }
            
            comment.setContenu(newText);
            try {
                commentaireService.updateCommentaire(comment);
                refreshCallback.run();
            } catch (SQLException e) {
                showError("Erreur lors de la modification du commentaire: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    private void handleDeleteComment(Commentaire comment) {
        try {
            commentaireService.deleteCommentaire(comment.getId());
            refreshCallback.run();
        } catch (SQLException e) {
            showError("Erreur lors de la suppression du commentaire");
        }
    }

    private void handleLikeComment(Commentaire comment) {
        try {
            commentaireService.likeCommentaire(comment.getId());
            refreshCallback.run();
        } catch (SQLException e) {
            showError("Erreur lors du like");
        }
    }

    private void handleDislikeComment(Commentaire comment) {
        try {
            commentaireService.dislikeCommentaire(comment.getId());
            refreshCallback.run();
        } catch (SQLException e) {
            showError("Erreur lors du dislike");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

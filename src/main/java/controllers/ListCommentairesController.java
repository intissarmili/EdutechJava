package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Commentaire;
import models.Feed;
import models.TranslatedComment;
import service.CommentaireService;
import service.TranslationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCommentairesController {

    @FXML private Label feedLabel;
    @FXML private VBox commentsContainer;
    @FXML private TextField commentField;
    @FXML private ComboBox<String> globalLanguageComboBox;

    private Feed feed;
    private final CommentaireService commentaireService = new CommentaireService();
    private final TranslationService translationService = new TranslationService();
    
    // Map to track translated comments
    private Map<Integer, TranslatedComment> translatedComments = new HashMap<>();
    
    @FXML
    public void initialize() {
        // Initialize the global language selector
        globalLanguageComboBox.getItems().addAll(translationService.getAvailableLanguages());
        globalLanguageComboBox.setValue(TranslationService.FRENCH);
        
        // When the global language changes, update all comments
        globalLanguageComboBox.setOnAction(e -> {
            String selectedLanguage = globalLanguageComboBox.getValue();
            updateAllCommentsLanguage(selectedLanguage);
        });
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
        feedLabel.setText("Publication : " + feed.getPublication());
        loadComments();
    }
    
    private void updateAllCommentsLanguage(String language) {
        // Update all comments to the selected language
        for (Map.Entry<Integer, TranslatedComment> entry : translatedComments.entrySet()) {
            entry.getValue().setCurrentLanguage(language);
        }
        
        // Reload the comments display
        loadComments();
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesByFeedId(feed.getId());
            
            // First create translated comments if they don't exist
            for (Commentaire c : commentaires) {
                if (!translatedComments.containsKey(c.getId())) {
                    TranslatedComment translatedComment = createTranslatedComment(c);
                    translatedComments.put(c.getId(), translatedComment);
                }
            }
            
            // Then display them
            for (Commentaire c : commentaires) {
                TranslatedComment translatedComment = translatedComments.get(c.getId());
                // Make sure to use the global language
                translatedComment.setCurrentLanguage(globalLanguageComboBox.getValue());
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
            if (!language.equals(TranslationService.FRENCH)) { // Skip French as it's the original
                String translatedText = translationService.translate(comment.getContenu(), language);
                translatedComment.addTranslation(language, translatedText);
            }
        }
        
        return translatedComment;
    }

    private void createCommentBox(TranslatedComment translatedComment) {
        Commentaire c = translatedComment.getOriginalComment();
        VBox commentBox = new VBox();
        commentBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-spacing: 4; -fx-background-radius: 6;");

        // Add a header with the original language and translation info
        HBox header = new HBox(5);
        Label originalLanguageLabel = new Label("Original: " + TranslationService.FRENCH);
        originalLanguageLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        Label translationLabel = new Label("Traduction: " + translatedComment.getCurrentLanguage());
        translationLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        header.getChildren().addAll(originalLanguageLabel, translationLabel);
        
        // Individual language selector for this comment
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll(translationService.getAvailableLanguages());
        languageSelector.setValue(translatedComment.getCurrentLanguage());
        languageSelector.setStyle("-fx-font-size: 10px;");
        
        // Content of the comment
        Label label = new Label(translatedComment.getCurrentText());
        label.setWrapText(true);
        
        // Action buttons
        HBox actions = new HBox(5);
        Button likeBtn = new Button("👍 " + c.getUpVotes());
        Button dislikeBtn = new Button("👎 " + c.getDownVotes());
        
        likeBtn.setOnAction(e -> handleLikeComment(c));
        dislikeBtn.setOnAction(e -> handleDislikeComment(c));
        
        actions.getChildren().addAll(likeBtn, dislikeBtn);
        
        // Update display when language changes
        languageSelector.setOnAction(e -> {
            String selectedLanguage = languageSelector.getValue();
            translatedComment.setCurrentLanguage(selectedLanguage);
            label.setText(translatedComment.getCurrentText());
            translationLabel.setText("Traduction: " + selectedLanguage);
        });
        
        commentBox.getChildren().addAll(header, languageSelector, label, actions);
        commentsContainer.getChildren().add(commentBox);
    }

    @FXML
    private void handleAddComment() {
        String text = commentField.getText().trim();
        if (!text.isEmpty()) {
            Commentaire c = new Commentaire();
            c.setContenu(text);
            c.setFeedId(feed.getId());
            try {
                commentaireService.addCommentaire(c);
                commentField.clear();
                
                // Add the new comment to our translations map
                TranslatedComment translatedComment = createTranslatedComment(c);
                translatedComments.put(c.getId(), translatedComment);
                
                loadComments();
            } catch (SQLException e) {
                showError("Erreur lors de l'ajout du commentaire");
            }
        }
    }
    
    private void handleLikeComment(Commentaire comment) {
        try {
            commentaireService.likeCommentaire(comment.getId());
            loadComments();
        } catch (SQLException e) {
            showError("Erreur lors du like");
        }
    }
    
    private void handleDislikeComment(Commentaire comment) {
        try {
            commentaireService.dislikeCommentaire(comment.getId());
            loadComments();
        } catch (SQLException e) {
            showError("Erreur lors du dislike");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListFeed.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) feedLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur lors du retour à la liste");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

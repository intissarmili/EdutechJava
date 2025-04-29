package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Commentaire;
import models.Feed;
import service.FeedService;
import service.CommentaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class FeedController {

    @FXML private VBox feedContainer;
    @FXML private TextArea newFeedTextArea;
    @FXML private Label feedLengthLabel;

    private final FeedService feedService = new FeedService();
    private final CommentaireService commentaireService = new CommentaireService();

    @FXML
    public void initialize() throws SQLException {
        loadFeeds();
    }


    @FXML
    private void checkFeedLength() {
        int length = newFeedTextArea.getText().length();
        feedLengthLabel.setText(length + "/14");

        if (length > 14) {
            feedLengthLabel.setStyle("-fx-text-fill: red;");
        } else {
            feedLengthLabel.setStyle("-fx-text-fill: grey;");
        }
    }


    private void loadFeeds() {
        feedContainer.getChildren().clear();
        try {
            List<Feed> feeds = feedService.getAllFeeds();

            for (Feed feed : feeds) {
                try {
                    // Chargement du template de publication
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/feed_item.fxml"));
                    VBox feedBox = loader.load();
                    FeedItemController controller = loader.getController();

                    // Configuration du contrôleur
                    controller.setFeed(feed);
                    controller.setFeedService(feedService);
                    controller.setCommentaireService(commentaireService);
                    controller.setRefreshCallback(this::loadFeeds); // Correction ici

                    feedContainer.getChildren().add(feedBox);
                } catch (IOException e) {
                    // Log l'erreur plutôt que printStackTrace()
                    System.err.println("Erreur lors du chargement du template FXML: " + e.getMessage());
                    createFeedBoxFallback(feed);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des feeds: " + e.getMessage());
            showError("Erreur lors du chargement des publications");
        }
    }

    private void createFeedBoxFallback(Feed feed) throws SQLException {
        VBox feedBox = new VBox(10);
        feedBox.setStyle("-fx-padding: 15; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fff;");

        Label feedLabel = new Label(feed.getPublication());
        feedLabel.setWrapText(true);
        feedLabel.setStyle("-fx-font-size: 14;");

        HBox feedActions = new HBox(10);
        Button btnDeleteFeed = new Button("🗑️ Supprimer");
        Button btnEditFeed = new Button("✏️ Modifier");
        feedActions.getChildren().addAll(btnEditFeed, btnDeleteFeed);

        feedBox.getChildren().addAll(feedLabel, feedActions);

        // Ajout des commentaires existants
        addCommentsToBox(feed, feedBox);

        // Formulaire d'ajout de commentaire
        addCommentForm(feed, feedBox);

        feedContainer.getChildren().add(feedBox);
    }

    private void addCommentsToBox(Feed feed, VBox feedBox) throws SQLException {
        List<Commentaire> commentaires = commentaireService.getCommentairesByFeedId(feed.getId());
        VBox commentsBox = new VBox(8);
        commentsBox.setStyle("-fx-padding: 10 0 0 20;");

        for (Commentaire c : commentaires) {
            HBox commentBox = new HBox(10);
            commentBox.setStyle("-fx-padding: 8; -fx-background-color: #f5f5f5; -fx-border-radius: 3;");

            Label commentLabel = new Label(c.getContenu());
            commentLabel.setWrapText(true);

            HBox commentActions = new HBox(5);
            Button btnEditComment = new Button("✏️");
            Button btnDeleteComment = new Button("🗑️");
            Button btnLike = new Button("👍 " + c.getUpVotes());
            Button btnDislike = new Button("👎 " + c.getDownVotes());
            commentActions.getChildren().addAll(btnLike, btnDislike, btnEditComment, btnDeleteComment);

            commentBox.getChildren().addAll(commentLabel, commentActions);
            commentsBox.getChildren().add(commentBox);

            // Configuration des actions
            setupCommentActions(c, btnEditComment, btnDeleteComment, btnLike, btnDislike);
        }

        feedBox.getChildren().add(commentsBox);
    }

    private void setupCommentActions(Commentaire c, Button btnEditComment, Button btnDeleteComment, Button btnLike, Button btnDislike) {
        btnEditComment.setOnAction(e -> editComment(c));
        btnDeleteComment.setOnAction(e -> deleteComment(c));
        btnLike.setOnAction(e -> likeComment(c));
        btnDislike.setOnAction(e -> dislikeComment(c));
    }

    private void addCommentForm(Feed feed, VBox feedBox) {
        HBox commentForm = new HBox(10);
        TextField newCommentField = new TextField();
        newCommentField.setPromptText("Ajouter un commentaire...");
        newCommentField.setStyle("-fx-pref-width: 300;");

        Button btnAddComment = new Button("Ajouter");
        btnAddComment.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnAddComment.setOnAction(e -> addComment(feed, newCommentField));

        commentForm.getChildren().addAll(newCommentField, btnAddComment);
        feedBox.getChildren().add(commentForm);
    }

    private void addComment(Feed feed, TextField newCommentField) {
        String contenu = newCommentField.getText().trim();
        if (!contenu.isEmpty()) {
            Commentaire newComment = new Commentaire();
            newComment.setContenu(contenu);
            newComment.setFeedId(feed.getId());
            try {
                commentaireService.addCommentaire(newComment);
                loadFeeds();
                newCommentField.clear();
            } catch (SQLException ex) {
                showError("Erreur lors de l'ajout du commentaire");
            }
        }
    }

    @FXML
    private void addFeed() {
        String content = newFeedTextArea.getText().trim();
        
        if (content.isEmpty()) {
            showError("Le contenu ne peut pas être vide");
            return;
        }
        
        if (content.length() > 14) {
            showError("Le contenu ne doit pas dépasser 14 caractères");
            return;
        }
        
        if (containsForbiddenWords(content)) {
            showError("Le contenu contient des mots interdits");
            return;
        }
        
        Feed newFeed = new Feed();
        newFeed.setPublication(content);
        newFeed.setLastModified(LocalDateTime.now());
        
        try {
            feedService.createFeed(newFeed);
            newFeedTextArea.clear();
            feedLengthLabel.setText("0/14");
            feedLengthLabel.setStyle("-fx-text-fill: grey;");
            loadFeeds();
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout de la publication: " + e.getMessage());
        }
    }

    private boolean containsForbiddenWords(String text) {
        String[] forbiddenWords = {"spam", "http://", "https://", "motinterdit"};
        for (String word : forbiddenWords) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    private void editComment(Commentaire c) {
        TextInputDialog dialog = new TextInputDialog(c.getContenu());
        dialog.setTitle("Modifier Commentaire");
        dialog.setHeaderText("Modifier le commentaire :");
        dialog.showAndWait().ifPresent(newText -> {
            c.setContenu(newText);
            try {
                commentaireService.updateCommentaire(c);
                loadFeeds();
            } catch (SQLException ex) {
                showError("Erreur lors de la modification du commentaire");
            }
        });
    }

    private void deleteComment(Commentaire c) {
        try {
            commentaireService.deleteCommentaire(c.getId());
            loadFeeds();
        } catch (SQLException ex) {
            showError("Erreur lors de la suppression du commentaire");
        }
    }

    private void likeComment(Commentaire c) {
        try {
            commentaireService.likeCommentaire(c.getId());
            loadFeeds();
        } catch (SQLException ex) {
            showError("Erreur lors du like");
        }
    }

    private void dislikeComment(Commentaire c) {
        try {
            commentaireService.dislikeCommentaire(c.getId());
            loadFeeds();
        } catch (SQLException ex) {
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

    @FXML
    private void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListFeed.fxml"));
            Parent root = loader.load();
            newFeedTextArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
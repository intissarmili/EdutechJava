package controllers.Back;

import controllers.ListCommentairesController;
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
import service.CommentaireService;
import service.FeedService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class FeedItemController {

    @FXML private Label feedContent;
    @FXML private VBox commentsContainer;
    @FXML private TextField newCommentField;
    @FXML private VBox feedItemBox;
    @FXML private Label commentLengthLabel;

    private Feed feed;
    private FeedService feedService = new FeedService();
    private CommentaireService commentaireService = new CommentaireService();


    private Runnable refreshCallback;


    @FXML
    private void checkCommentLength() {
        int length = newCommentField.getText().length();
        commentLengthLabel.setText(length + "/10");

        if (length > 10) {
            commentLengthLabel.setStyle("-fx-text-fill: red;");
        } else {
            commentLengthLabel.setStyle("-fx-text-fill: grey;");
        }
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
        feedContent.setText(feed.getPublication());
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
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesByFeedId(feed.getId());
            for (Commentaire comment : commentaires) {
                createCommentBox(comment);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires");
        }
    }

    private void createCommentBox(Commentaire comment) {
        HBox commentBox = new HBox(10);
        commentBox.setStyle("-fx-padding: 8; -fx-background-color: #f5f5f5; -fx-border-radius: 3;");

        Label commentLabel = new Label(comment.getContenu());
        commentLabel.setWrapText(true);

        HBox actionsBox = new HBox(5);
        Button btnLike = new Button("👍 " + comment.getUpVotes());
        Button btnDislike = new Button("👎 " + comment.getDownVotes());
        Button btnEdit = new Button("✏️");
        Button btnDelete = new Button("🗑️");

        btnEdit.setOnAction(e -> handleEditComment(comment));
        btnDelete.setOnAction(e -> handleDeleteComment(comment));
        btnLike.setOnAction(e -> handleLikeComment(comment));
        btnDislike.setOnAction(e -> handleDislikeComment(comment));

        actionsBox.getChildren().addAll(btnLike, btnDislike, btnEdit, btnDelete);
        commentBox.getChildren().addAll(commentLabel, actionsBox);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Back/ListCommentaires.fxml"));
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
    private void handleEditFeed() {
        TextInputDialog dialog = new TextInputDialog(feed.getPublication());
        dialog.setTitle("Modifier Publication");
        dialog.setHeaderText("Modifier la publication :");
        dialog.showAndWait().ifPresent(newText -> {
            feed.setPublication(newText);
            try {
                feedService.updateFeed(feed);
                refreshCallback.run();
            } catch (SQLException e) {
                showError("Erreur lors de la modification de la publication");
            }
        });
    }

    @FXML
    private void handleDeleteFeed() {
        try {
            feedService.deleteFeed(feed.getId());
            refreshCallback.run();
        } catch (SQLException e) {
            showError("Erreur lors de la suppression de la publication");
        }
    }

    @FXML
    private void handleAddComment() {
        String contenu = newCommentField.getText().trim();

        if (contenu.isEmpty()) {
            showError("Veuillez saisir un commentaire");
            return;
        }

        if (contenu.length() > 10) {
            showError("Le commentaire ne doit pas dépasser 10 caractères");
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
            comment.setContenu(newText);
            try {
                commentaireService.updateCommentaire(comment);
                refreshCallback.run();
            } catch (SQLException e) {
                showError("Erreur lors de la modification du commentaire");
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

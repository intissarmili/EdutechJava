package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Commentaire;
import models.Feed;
import service.CommentaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListCommentairesController {

    @FXML private Label feedLabel;
    @FXML private VBox commentsContainer;
    @FXML private TextField commentField;

    private Feed feed;
    private final CommentaireService commentaireService = new CommentaireService();

    public void setFeed(Feed feed) {
        this.feed = feed;
        feedLabel.setText("Publication : " + feed.getPublication());
        loadComments();
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesByFeedId(feed.getId());
            for (Commentaire c : commentaires) {
                VBox commentBox = new VBox();
                commentBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 8; -fx-spacing: 4; -fx-background-radius: 6;");

                Label label = new Label(c.getContenu());
                label.setWrapText(true);
                commentBox.getChildren().add(label);

                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires");
        }
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
                loadComments();
            } catch (SQLException e) {
                showError("Erreur lors de l'ajout du commentaire");
            }
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

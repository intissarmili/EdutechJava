package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Feed;
import service.FeedService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ListFeedController {

    @FXML
    private TableView<Feed> tableFeeds;
    @FXML
    private TableColumn<Feed, String> colFeedText;
    @FXML
    private TableColumn<Feed, Void> colActions;

    private final FeedService feedService = new FeedService();

    @FXML
    public void initialize() {
        try {
            List<Feed> feeds = feedService.getAllFeeds();
            ObservableList<Feed> observableFeeds = FXCollections.observableArrayList(feeds);
            tableFeeds.setItems(observableFeeds);

            colFeedText.setCellValueFactory(new PropertyValueFactory<>("publication"));
            addActionButtonsToTable();

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des publications : " + e.getMessage());
        }
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox pane = new HBox(btnEdit, btnDelete);

            {
                pane.setSpacing(10);

                btnEdit.setOnAction(event -> {
                    Feed selectedFeed = getTableView().getItems().get(getIndex());
                    showEditDialog(selectedFeed);
                });

                btnDelete.setOnAction(event -> {
                    Feed selectedFeed = getTableView().getItems().get(getIndex());
                    System.out.println("Tentative de suppression de la publication ID: " + selectedFeed.getId());
                    deleteFeed(selectedFeed);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void showEditDialog(Feed feed) {
        TextInputDialog dialog = new TextInputDialog(feed.getPublication());
        dialog.setTitle("Modifier la publication");
        dialog.setHeaderText("Éditez la publication :");

        dialog.showAndWait().ifPresent(newText -> {
            feed.setPublication(newText);
            try {
                feedService.updateFeed(feed);
                refreshTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteFeed(Feed feed) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Voulez-vous vraiment supprimer cette publication ?");
        confirmationAlert.setContentText("Attention : Tous les commentaires associés seront également supprimés !");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = feedService.deleteFeed(feed.getId());
                if (deleted) {
                    refreshTable();

                    // Afficher un message de succès (optionnel)
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Publication supprimée avec succès !");
                    successAlert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Avertissement");
                    alert.setHeaderText(null);
                    alert.setContentText("Aucune publication n'a été trouvée avec cet identifiant.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de base de données");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la suppression : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void refreshTable() {
        try {
            List<Feed> feeds = feedService.getAllFeeds();
            if (feeds != null) {
                tableFeeds.getItems().clear(); // Assurez-vous de vider la table avant de la recharger
                tableFeeds.setItems(FXCollections.observableArrayList(feeds));
                System.out.println("Table rechargée avec " + feeds.size() + " publications");
            } else {
                System.out.println("Aucune publication trouvée ou liste null");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de rafraîchir la liste : " + e.getMessage());
        }
    }



    @FXML
    private void goToCreateFeed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/feed_commentaire.fxml")); // Remplace par le nom correct de ton fichier FXML
            ScrollPane feedRoot = loader.load();

            // Récupère la scène actuelle et remplace le root
            tableFeeds.getScene().setRoot(feedRoot);

        } catch (IOException e) {
            e.printStackTrace();
            // Optionnel : afficher une alerte d’erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger la page de création.");
            alert.showAndWait();
        }
    }

}

package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import models.Abonnement;
import services.AbonnementService;

public class ListeAbonnementController {

    @FXML
    private TableView<Abonnement> tableAbonnements;

    @FXML
    private TableColumn<Abonnement, String> colNom;

    @FXML
    private TableColumn<Abonnement, String> colDuree;

    @FXML
    private TableColumn<Abonnement, String> colPrix;

    @FXML
    private TableColumn<Abonnement, String> colDescription;

    @FXML
    private TableColumn<Abonnement, Void> colActions; // Nouvelle colonne pour les boutons

    private AbonnementService abonnementService;

    public ListeAbonnementController() {
        abonnementService = new AbonnementService();
    }

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(cellData -> cellData.getValue().nomProperty());

        colDuree.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDuree() + " mois")
        );

        colPrix.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPrix() + " DT")
        );

        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        // Charger les données
        tableAbonnements.setItems(abonnementService.getAllAbonnements());

        // Ajouter les boutons Modifier/Supprimer pour chaque ligne
        addActionButtonsToTable();
    }

    private void addActionButtonsToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(10, btnModifier, btnSupprimer);

            {
                pane.setPadding(new Insets(5));
                pane.setAlignment(Pos.CENTER);

                // Style en Java pour le bouton "Modifier" (vert)
                btnModifier.setStyle(
                        "-fx-background-color: #4CAF50;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;"
                );

                // Style en Java pour le bouton "Supprimer" (rouge)
                btnSupprimer.setStyle(
                        "-fx-background-color: #F44336;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;"
                );

                btnModifier.setOnAction(event -> {
                    Abonnement abonnement = getTableView().getItems().get(getIndex());
                    handleUpdate(abonnement);
                });

                btnSupprimer.setOnAction(event -> {
                    Abonnement abonnement = getTableView().getItems().get(getIndex());
                    handleDelete(abonnement);
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




    private void handleUpdate(Abonnement abonnement) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Abonnement");

        // Créer les champs
        TextField nomField = new TextField(abonnement.getNom());
        TextField dureeField = new TextField(String.valueOf(abonnement.getDuree()));
        TextField prixField = new TextField(String.valueOf(abonnement.getPrix()));
        TextField descriptionField = new TextField(abonnement.getDescription());

        // Layout des champs
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);

        grid.add(new Label("Durée (mois):"), 0, 1);
        grid.add(dureeField, 1, 1);

        grid.add(new Label("Prix (DT):"), 0, 2);
        grid.add(prixField, 1, 2);

        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Ajouter les boutons OK et Annuler
        ButtonType updateButtonType = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == updateButtonType) {
                try {
                    String nouveauNom = nomField.getText().trim();
                    int nouvelleDuree = Integer.parseInt(dureeField.getText().trim());
                    double nouveauPrix = Double.parseDouble(prixField.getText().trim());
                    String nouvelleDescription = descriptionField.getText().trim();

                    if (nouveauNom.isEmpty() || nouvelleDescription.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les champs ne doivent pas être vides.");
                        return;
                    }

                    abonnement.setNom(nouveauNom);
                    abonnement.setDuree(String.valueOf(nouvelleDuree));
                    abonnement.setPrix(String.valueOf(nouveauPrix));
                    abonnement.setDescription(nouvelleDescription);

                    abonnementService.updateAbonnement(abonnement, abonnement);
                    tableAbonnements.refresh();
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de format", "Durée et Prix doivent être des nombres valides.");
                }
            }
        });
    }


    private void handleDelete(Abonnement abonnement) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet abonnement ?");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationAlert.getButtonTypes().setAll(okButton, cancelButton);

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == okButton) {
                abonnementService.deleteAbonnement(abonnement);
                tableAbonnements.getItems().remove(abonnement);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

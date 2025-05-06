package controller;



import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Abonnement;
import services.AbonnementService;

import java.io.IOException;

public class ListeAbonnementsController {

    @FXML
    private TableView<Abonnement> abonnementTable;

    @FXML
    private TableColumn<Abonnement, String> nomColumn;
    @FXML
    private TableColumn<Abonnement, String> dureeColumn;
    @FXML
    private TableColumn<Abonnement, String> prixColumn;
    @FXML
    private TableColumn<Abonnement, String> descriptionColumn;

    private final AbonnementService abonnementService = new AbonnementService();

    public void initialize() {
        // Configure les colonnes

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Récupère la liste des abonnements et l'affiche dans la table
        ObservableList<Abonnement> abonnements = abonnementService.getAllAbonnements();
        if (abonnements.isEmpty()) {
            System.out.println("Aucun abonnement à afficher.");
        } else {
            System.out.println("Abonnements affichés : " + abonnements.size());
        }
        abonnementTable.setItems(abonnements);
    }


    @FXML
    public void handleRetour() throws IOException {
        // Retourner à la vue précédente
        Stage stage = (Stage) abonnementTable.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterAbonnement.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
    }
}

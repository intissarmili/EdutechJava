package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import models.Certification;
import services.PanierService;
import services.PointsService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static controller.PDFViewer.openPDF;

public class PanierController {

    @FXML
    private ListView<Certification> panierListView;

    @FXML
    private Label pointsRestantsLabel;

    @FXML
    private Button backToShoppingButton;

    @FXML
    private Button viderPanierButton;

    @FXML
    private Button payerButton;  // Ajouter un bouton pour payer

    @FXML
    private Label totalPointsLabel;

    private final PointsService pointsService = new PointsService();
    private final int userId = 1; // Remplacer si nécessaire
    private int soldeActuel; // solde initial de l'utilisateur
    private double reduction = 0.10; // 10% de réduction

    @FXML
    private void initialize() {

        // Charger certifications dans le panier
        List<Certification> certificationsDansPanier = PanierService.getCertificationsDansPanier();
        panierListView.setItems(FXCollections.observableArrayList(certificationsDansPanier));

        // Charger solde
        soldeActuel = pointsService.getUserPoints(userId);

        // Initialiser points restants
        updateTotal();

        panierListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Certification> call(ListView<Certification> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Certification certif, boolean empty) {
                        super.updateItem(certif, empty);

                        if (empty || certif == null) {
                            setGraphic(null);
                        } else {
                            Label nameLabel = new Label(certif.getNom());
                            nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                            Label prixText = new Label("Prix :");
                            prixText.setStyle("-fx-font-size: 12px;");
                            Label prixLabel = new Label(certif.getPrix() + " points");
                            prixLabel.setStyle("-fx-font-size: 12px;");

                            VBox infoBox = new VBox(5, nameLabel, prixText, prixLabel);

                            Button deleteButton = new Button("🗑");
                            deleteButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20px;");
                            deleteButton.setOnAction(e -> {
                                PanierService.supprimerCertification(certif);
                                getListView().getItems().remove(certif);
                                updateTotal();
                            });

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            HBox hBox = new HBox(10, infoBox, spacer, deleteButton);
                            hBox.setStyle("-fx-padding: 10px; -fx-alignment: CENTER_LEFT;");
                            setGraphic(hBox);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleBackToShopping(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherCertifications.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleViderPanier() {
        PanierService.viderPanier(); // méthode à implémenter dans PanierService si pas encore faite
        panierListView.getItems().clear();
        updateTotal();
    }

    @FXML
    private void handlePayer() {
        double total = 0;
        List<Certification> certificationsDansPanier = panierListView.getItems();

        // Calculer le total du panier
        for (Certification certif : certificationsDansPanier) {
            total += certif.getPrix();
        }

        // Appliquer la réduction seulement si le panier contient 3 certifications ou plus
        double totalAvecReduction = total;
        if (certificationsDansPanier.size() >= 3) {
            double reduction = 0.10; // 10% de réduction
            totalAvecReduction = total - (total * reduction);
        }

        // Vérifier si l'utilisateur a suffisamment de points
        if (soldeActuel >= totalAvecReduction) {
            pointsService.deduirePoints(userId, (int) totalAvecReduction);
            soldeActuel -= totalAvecReduction;

            if (certificationsDansPanier.size() >= 3) {
                showAlert("Paiement réussi", "Votre achat a été effectué avec 10% de réduction !");
            } else {
                showAlert("Paiement réussi", "Votre achat a été effectué sans réduction.");
            }

            if (!certificationsDansPanier.isEmpty()) {
                Certification certifAchetee = certificationsDansPanier.get(0);

                String imagePath = getClass().getResource("/images/" + certifAchetee.getImg()).toExternalForm();
                if (imagePath != null) {
                    PDFViewerController.createCertificationPDF(certifAchetee.getNom(), certifAchetee.getDescription(), imagePath);
                    openPDF("Certification_" + certifAchetee.getNom() + ".pdf");
                } else {
                    showAlert("Erreur", "L'image de la certification est introuvable.");
                }
            }

            PanierService.viderPanier();
            panierListView.getItems().clear();
            updateTotal();
        } else {
            showAlert("Points insuffisants", "Vous n'avez pas assez de points pour effectuer cet achat.");
        }
        if (certificationsDansPanier.size() >= 3) {
            showReductionAlert(total, totalAvecReduction);
        } else {
            showAlert("Paiement réussi", "Votre achat a été effectué sans réduction.");
        }

    }
    private void showReductionAnnonce() {
        // Créer une fenêtre de dialogue modale
        Dialog<Void> announcementDialog = new Dialog<>();
        announcementDialog.setTitle("🎉 Réduction appliquée !");

        // Créer un VBox pour organiser les éléments
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f8ff;");

        // Icône 🎉
        Label icon = new Label("🎉");
        icon.setStyle("-fx-font-size: 48px;");

        // Message principal
        Label message = new Label("Félicitations !");
        message.setStyle("-fx-font-size: 24px; -fx-text-fill: #0a74da; -fx-font-weight: bold;");

        // Message secondaire
        Label subMessage = new Label("Vous avez droit à une réduction de 10% !");
        subMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #0a74da;");

        // Bouton de fermeture
        Button btnClose = new Button("Super, je continue !");
        btnClose.setStyle("-fx-background-color: #0a74da; -fx-text-fill: white; -fx-padding: 10 20;");
        btnClose.setOnAction(e -> announcementDialog.close());  // Fermer la fenêtre lorsque l'utilisateur clique sur le bouton

        root.getChildren().addAll(icon, message, subMessage, btnClose);

        // Définir le contenu du dialog
        announcementDialog.getDialogPane().setContent(root);

        // Définir le bouton par défaut (ce qui permet de fermer la fenêtre via le bouton)
        announcementDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Afficher la fenêtre modale
        announcementDialog.showAndWait();  // Cela empêche toute interaction avec d'autres fenêtres jusqu'à ce que l'utilisateur ferme celle-ci
    }



    private void showReductionAlert(double totalAvant, double totalApres) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Félicitations pour votre achat !");
        alert.setHeaderText("Vous avez bénéficié d'une réduction exclusive !");
        alert.setContentText(
                "✅ Vous avez ajouté 3 certifications ou plus à votre panier.\n\n" +
                        "💸 Total avant réduction : " + String.format("%.0f", totalAvant) + " points\n" +
                        "🎁 Total après réduction : " + String.format("%.0f", totalApres) + " points\n\n" +
                        "Merci de votre confiance 🙏.\n" +
                        "Continuez à apprendre et à grandir avec nous ! 🚀"
        );
        alert.showAndWait();
    }





    // Génération du PDF avec iText 5
    private void generatePDF(List<Certification> certifications) {
        Document document = new Document();
        try {
            // Créer un fichier de sortie pour le PDF
            PdfWriter.getInstance(document, new FileOutputStream("Certification_Achat_Succes.pdf"));

            // Ouvrir le document pour ajouter du contenu
            document.open();

            // Ajouter un titre
            document.add(new Paragraph("Certifications achetées", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));

            // Ajouter les détails de chaque certification achetée
            for (Certification certif : certifications) {
                document.add(new Paragraph("Nom : " + certif.getNom()));
                document.add(new Paragraph("Description : " + certif.getDescription()));
                document.add(new Paragraph("Prix : " + certif.getPrix() + " points"));
                document.add(new Paragraph("\n"));
            }

            // Fermer le document
            document.close();
            System.out.println("Le PDF a été généré avec succès !");
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    // Afficher un message d'alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTotal() {
        double total = 0;
        List<Certification> certificationsDansPanier = panierListView.getItems();

        for (Certification c : certificationsDansPanier) {
            total += c.getPrix();
        }

        double totalAvecReduction = total;
        boolean reductionAppliquee = false;

        if (certificationsDansPanier.size() >= 3) {
            totalAvecReduction = total - (total * reduction); // Seulement sur le total
            reductionAppliquee = true;
        }

        totalPointsLabel.setText(String.format("Total : %.0f points", totalAvecReduction));

        // Points restants = solde initial - totalAvecReduction
        int pointsRestants = (int) (soldeActuel - totalAvecReduction);
        pointsRestantsLabel.setText("Points restants : " + pointsRestants + " points");

        // Afficher bande annonce quand on atteint 3 certifications
        if (certificationsDansPanier.size() == 3 && reductionAppliquee) {
            showReductionAnnonce();
        }
    }

}

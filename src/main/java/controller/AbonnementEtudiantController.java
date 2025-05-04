package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField; // <= ajout important
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import models.Abonnement;
import services.AbonnementService;
import javafx.collections.ObservableList;
import services.StripeService;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class AbonnementEtudiantController {

    @FXML
    private FlowPane abonnementsPane;

    @FXML
    private WebView paymentWebView;

    @FXML
    private TextField searchField; // <= ton champ de recherche

    private final AbonnementService abonnementService = new AbonnementService();

    @FXML
    public void initialize() {
        setupSearchField(); // <= écouter la recherche
        loadAbonnements();
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAbonnements(newValue);
        });
    }

    private void loadAbonnements() {
        ObservableList<Abonnement> abonnements = abonnementService.getAllAbonnements();

        if (abonnements.isEmpty()) {
            showAlert("Aucun abonnement disponible", "Veuillez réessayer plus tard.");
            return;
        }

        abonnementsPane.getChildren().clear();

        for (Abonnement abonnement : abonnements) {
            VBox card = createAbonnementCard(abonnement);
            abonnementsPane.getChildren().add(card);
        }
    }

    private void filterAbonnements(String query) {
        ObservableList<Abonnement> abonnements = abonnementService.getAllAbonnements();
        abonnementsPane.getChildren().clear();

        for (Abonnement abonnement : abonnements) {
            if (abonnement.getNom().toLowerCase().contains(query.toLowerCase())) {
                VBox card = createAbonnementCard(abonnement);
                abonnementsPane.getChildren().add(card);
            }
        }
    }

    private VBox createAbonnementCard(Abonnement abonnement) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(250);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #90caf9; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(33,150,243,0.2), 10, 0, 0, 5);"
        );

        Label title = new Label(abonnement.getNom());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");

        // Image de livre dans un cercle
        ImageView bookIcon = new ImageView(new Image(getClass().getResource("/images/img_3.png").toExternalForm()));
        bookIcon.setFitWidth(80);
        bookIcon.setFitHeight(80);
        bookIcon.setPreserveRatio(true);
        bookIcon.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #90caf9; " +
                        "-fx-border-radius: 50%; " +
                        "-fx-background-radius: 50%;" // Pas directement applicable sur ImageView mais on peut le faire via StackPane si tu veux
        );

        Label price = new Label(abonnement.getPrix() + " DT");
        price.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1976d2;");

        Label duration = new Label(abonnement.getDuree() + " mois");
        duration.setStyle("-fx-font-size: 14px; -fx-text-fill: #64b5f6;");

        Label description = new Label(abonnement.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 13px; -fx-text-fill: #90caf9;");

        Button btnAcheter = new Button("Acheter");
        btnAcheter.setStyle(
                "-fx-background-color: #2196f3; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 20px; " +
                        "-fx-padding: 8px 16px;"
        );
        btnAcheter.setOnAction(e -> handlePurchase(abonnement));

        card.getChildren().addAll(title, bookIcon, price, duration, description, btnAcheter);
        return card;
    }

    private void handlePurchase(Abonnement abonnement) {
        try {
            StripeService stripeService = new StripeService();

            double tauxTndVersEur = 0.30; // 1 TND = 0.30 EUR
            double prixTnd = Double.parseDouble(abonnement.getPrix());
            double prixEur = prixTnd * tauxTndVersEur;
            long amountInCents = (long) (prixEur * 100);

            String paymentUrl = stripeService.createCheckoutSession(amountInCents, "eur");

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Paiement Stripe");

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.load(paymentUrl);

            webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.contains("success")) {
                    paymentStage.close();
                    showSuccessPage();
                } else if (newValue.contains("cancel")) {
                    paymentStage.close();
                    showCancelPage();
                }
            });

            Scene scene = new Scene(webView, 800, 600);
            paymentStage.setScene(scene);
            paymentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur de paiement");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Le paiement a échoué. Veuillez réessayer.");
            errorAlert.showAndWait();
        }
    }

    private void showSuccessPage() {
        Stage successStage = new Stage();
        successStage.setTitle("Paiement Réussi");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f8ff;");

        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("Merci pour votre achat !");
        message.setStyle("-fx-font-size: 24px; -fx-text-fill: #0a74da; -fx-font-weight: bold;");

        Button btnClose = new Button("Retour");
        btnClose.setStyle("-fx-background-color: #0a74da; -fx-text-fill: white; -fx-padding: 10 20;");
        btnClose.setOnAction(e -> successStage.close());

        root.getChildren().addAll(icon, message, btnClose);

        Scene scene = new Scene(root, 400, 300);
        successStage.setScene(scene);
        successStage.show();
    }

    private void showCancelPage() {
        Stage cancelStage = new Stage();
        cancelStage.setTitle("Paiement Annulé");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8d7da;");

        Label icon = new Label("❌");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("Le paiement a été annulé !");
        message.setStyle("-fx-font-size: 24px; -fx-text-fill: #721c24; -fx-font-weight: bold;");

        Button btnClose = new Button("Retour");
        btnClose.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 10 20;");
        btnClose.setOnAction(e -> cancelStage.close());

        root.getChildren().addAll(icon, message, btnClose);

        Scene scene = new Scene(root, 400, 300);
        cancelStage.setScene(scene);
        cancelStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

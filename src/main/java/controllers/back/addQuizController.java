package controllers.back;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Quiz;
import service.CoursService;
import service.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class addQuizController {

    // Composants FXML
    @FXML private TextField prixPieceField;
    @FXML private ComboBox<Integer> coursComboBox;
    @FXML private Button cancelBtn;
    @FXML private Button addBtn;

    // Services
    private final QuizService quizService = new QuizService();
    private final CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        setupControles();
        chargerDonneesInitiales();
    }

    private void setupControles() {
        configurerBoutons();
        configurerComboBox();
    }

    private void configurerBoutons() {
        cancelBtn.setOnAction(event -> fermerFenetre());
        addBtn.setOnAction(event -> handleAjouterQuiz());
    }

    private void configurerComboBox() {
        coursComboBox.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                setText(empty ? null : "Cours #" + id);
            }
        });

        coursComboBox.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                setText(empty ? null : "Cours #" + id);
            }
        });
    }

    private void chargerDonneesInitiales() {
        chargerIdsCoursDisponibles();
    }

    private void chargerIdsCoursDisponibles() {
        try {
            List<Integer> idsCours = coursService.getAllCoursIds();
            coursComboBox.setItems(FXCollections.observableArrayList(idsCours));
        } catch (SQLException e) {
            afficherAlerte("Erreur BD", "Chargement impossible",
                    "Erreur lors du chargement des cours : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAjouterQuiz() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            Quiz nouveauQuiz = creerQuizDepuisFormulaire();
            quizService.create(nouveauQuiz);
            afficherConfirmation();
            redirigerVersListeQuiz();
        } catch (SQLException e) {
            gererErreurBD(e);
        } catch (NumberFormatException e) {
            afficherAlerte("Format invalide", "Erreur de saisie",
                    "Le prix doit être un nombre valide",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        validerPrixPiece(erreurs);
        validerSelectionCours(erreurs);

        if (erreurs.length() > 0) {
            afficherAlerte("Validation", "Erreurs détectées",
                    erreurs.toString(),
                    Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void validerPrixPiece(StringBuilder erreurs) {
        try {
            int prix = Integer.parseInt(prixPieceField.getText());
            if (prix < 1 || prix > 700) {
                erreurs.append("- Prix doit être entre 1 et 700\n");
            }
        } catch (NumberFormatException e) {
            erreurs.append("- Prix invalide (nombre requis)\n");
        }
    }

    private void validerSelectionCours(StringBuilder erreurs) {
        if (coursComboBox.getValue() == null) {
            erreurs.append("- Sélectionnez un cours\n");
        }
    }

    private Quiz creerQuizDepuisFormulaire() {
        return new Quiz(
                Integer.parseInt(prixPieceField.getText()),
                coursComboBox.getValue()
        );
    }

    private void afficherConfirmation() {
        afficherAlerte("Succès", "Ajout réussi",
                "Le quiz a été ajouté avec succès",
                Alert.AlertType.INFORMATION);
    }

    private void gererErreurBD(SQLException e) {
        String message = e.getMessage().contains("foreign key") ?
                "Le cours sélectionné n'existe plus" :
                "Erreur technique : " + e.getMessage();

        afficherAlerte("Erreur BD", "Problème de persistance",
                message,
                Alert.AlertType.ERROR);
    }

    private void afficherAlerte(String titre, String entete, String contenu, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(entete);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    private void redirigerVersListeQuiz() {
        try {
            fermerFenetre();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Back/AffichierQuiz.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Quiz");
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Navigation", "Erreur de chargement",
                    "Impossible d'ouvrir la liste des quiz",
                    Alert.AlertType.ERROR);
        }
    }
}

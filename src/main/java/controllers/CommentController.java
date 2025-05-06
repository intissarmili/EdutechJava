package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Comment;
import models.User;
import service.AuthService;
import service.CommentService;

import utils.CommentValidator;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommentController {
    @FXML
    private TextArea commentArea;
    
    @FXML
    private TableView<Comment> commentsTable;
    
    @FXML
    private TableColumn<Comment, String> userColumn;
    
    @FXML
    private TableColumn<Comment, String> contentColumn;
    
    @FXML
    private TableColumn<Comment, String> dateColumn;


    private final AuthService authService;
    private final CommentService commentService;
    private List<Comment> comments = new ArrayList<>();

    public CommentController() {

        this.authService = AuthService.getInstance();
        this.commentService = new CommentService();
        
        // Créer un utilisateur de test si aucun utilisateur n'est connecté
       // if (!authService.isUserLoggedIn()) {
           // User testUser = new User(1, "TestUser", "test@example.com");
           // authService.setCurrentUser(testUser);
           // System.out.println("Utilisateur de test créé: " + testUser.getUsername());
       // }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadComments();
        System.out.println("CommentController initialisé");
    }
    
    private void setupTableColumns() {
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
    }
    
    private void loadComments() {
        try {
            // Charger les commentaires depuis la base de données
            comments = commentService.getAllComments();
            commentsTable.getItems().setAll(comments);
            System.out.println("Commentaires chargés: " + comments.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des commentaires: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, 
                     "Erreur", 
                     "Impossible de charger les commentaires: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmitComment() {
        System.out.println("Tentative de soumission d'un commentaire");
        
        // Vérifier si l'utilisateur est connecté
        if (!authService.isUserLoggedIn()) {
            System.out.println("Aucun utilisateur connecté");
            showAlert(Alert.AlertType.WARNING, 
                     "Non connecté", 
                     "Vous devez être connecté pour publier un commentaire.");
            return;
        }
        
        String commentText = commentArea.getText();
        System.out.println("Texte du commentaire: " + commentText);
        
        if (commentText == null || commentText.trim().isEmpty()) {
            System.out.println("Commentaire vide");
            showAlert(Alert.AlertType.WARNING, 
                     "Commentaire vide", 
                     "Veuillez écrire un commentaire avant de soumettre.");
            return;
        }

        // Vérifier si le commentaire contient du contenu interdit
        boolean hasForbiddenContent = CommentValidator.containsForbiddenContent(commentText);
        System.out.println("Contient du contenu interdit: " + hasForbiddenContent);
        
        if (hasForbiddenContent) {
            User currentUser = authService.getCurrentUser();
            System.out.println("Envoi d'email d'avertissement à: " + currentUser.getEmail());
            
            // Envoyer un email d'avertissement
           // emailService.sendWarningEmail(
               // currentUser.getEmail(),
                //currentUser.getUsername(),
               // commentText
            //);

            showAlert(Alert.AlertType.WARNING, 
                     "Contenu inapproprié", 
                     "Votre commentaire contient du contenu inapproprié. Un email d'avertissement vous a été envoyé.");
            
            // Effacer le commentaire
            commentArea.clear();
            return;
        }

        // Si le commentaire est valide, le sauvegarder
        try {
           // User currentUser = authService.getCurrentUser();
            //System.out.println("Sauvegarde du commentaire pour l'utilisateur: " + currentUser.getUsername());
            
            Comment comment = new Comment();
            comment.setContent(commentText);
            //comment.setUsername(currentUser.getUsername());
            comment.setDate(LocalDateTime.now());
            
            // Sauvegarder le commentaire dans la base de données
            commentService.createComment(comment);
            
            // Recharger les commentaires
            loadComments();
            
            showAlert(Alert.AlertType.INFORMATION, 
                     "Succès", 
                     "Votre commentaire a été publié avec succès.");
            commentArea.clear();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du commentaire: " + e.getMessage());
            e.printStackTrace();
            
            showAlert(Alert.AlertType.ERROR, 
                     "Erreur", 
                     "Une erreur est survenue lors de la publication du commentaire : " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        commentArea.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        System.out.println("Affichage d'une alerte: " + title + " - " + content);
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
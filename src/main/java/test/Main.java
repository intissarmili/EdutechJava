package test;

import models.Commentaire;
import models.Feed;
import service.CommentaireService;
import service.FeedService;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Connection connection;
        connection = MaConnexion.getInstance().getConnection();


        System.out.println(connection);
        FeedService fs = new FeedService();
        CommentaireService cs = new CommentaireService();

        try {
            // 1. Ajouter un nouveau feed
            fs.addFeed(new Feed("saluuuuuuuuuuuuuuut tous le monde"));

            // 2. Récupérer ce feed pour avoir son ID
            Feed lastFeed = fs.getLastFeed();
            if (lastFeed != null) {
                // 3. Ajouter un commentaire avec le bon feed_id
                cs.addCommentaire(new Commentaire("helloooooooooooooo les enfants", lastFeed.getId()));
            } else {
                System.out.println("Aucun feed trouvé.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
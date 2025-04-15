package org.example;

import models.Cours;
import models.Quiz;
import services.CoursService;
import services.QuizService;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Connection conn = MyConnection.getInstance().getConnection();
        System.out.println("Connexion établie: " + (conn != null));
        Scanner scanner = new Scanner(System.in);

        try {
            // 1. Demander l'ID d'une certification existante
            System.out.print("Entrez l'ID d'une certification existante: ");
            int certificationId = scanner.nextInt();

            // 2. Création cours (auto-increment)
            CoursService coursService = new CoursService();
            Cours cours = new Cours(
                    "Cours Test",
                    "contenu_test.pdf",
                    "Test",
                    certificationId
            );
            coursService.create(cours);
            System.out.println("Cours créé avec ID: " + cours.getId());


        } catch (SQLException e) {
            System.err.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
            scanner.close();
        }
    }
}
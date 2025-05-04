package services;

import models.Certification;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class PanierService {

    private static final List<Certification> certificationsDansPanier = new ArrayList<>();

    public PanierService(Connection conn) {
    }

    public PanierService() {

    }

    public static List<Certification> getCertificationsDansPanier() {
        return certificationsDansPanier;
    }

    public static void ajouterCertification(Certification certif) {
        certificationsDansPanier.add(certif);
    }

    public static void supprimerCertification(Certification certif) {
        certificationsDansPanier.remove(certif);
    }

    public static void viderPanier() {
        certificationsDansPanier.clear(); // ou la liste que tu utilises
    }
    private static int pointsRestants = -1; // -1 par défaut pour non initialisé

    // Méthode pour définir les points restants
    public static void setPointsRestants(int points) {
        pointsRestants = points;
    }

    // Méthode pour récupérer les points restants
    public static int getPointsRestants() {
        return pointsRestants;
    }
    // Cette méthode retourne la première certification du panier
    public static Certification getCertificationAchetee() {
        if (!certificationsDansPanier.isEmpty()) {
            // Par exemple, prendre la première certification du panier comme achetée
            return certificationsDansPanier.get(0); // Vous pouvez adapter cette logique en fonction de vos besoins
        }
        return null; // Aucun produit dans le panier
    }
    // Méthode pour calculer le total avec réduction si plus de 3 certifications
    public static double calculerTotalAvecReduction() {
        double total = 0;

        for (Certification certif : certificationsDansPanier) {
            total += certif.getPrix();
        }

        if (certificationsDansPanier.size() > 3) {
            total *= 0.9; // appliquer 10% de réduction
        }

        return total;
    }

}


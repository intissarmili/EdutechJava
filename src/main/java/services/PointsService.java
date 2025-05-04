package services;

public class PointsService {

    // Simulation : solde de points de l'utilisateur (en entier)
    private int userPoints = 550; // Valeur initiale (peut être changée)

    // Méthode pour obtenir les points de l'utilisateur
    public int getPoints(int userId) {
        // Normalement, ici tu irais chercher depuis la base de données
        return userPoints;
    }

    // Méthode pour obtenir les points (alias de getPoints pour cohérence)
    public int getUserPoints(int userId) {
        return userPoints;
    }

    // Méthode pour déduire des points
    public boolean deduirePoints(int userId, int points) {
        if (userPoints >= points) {
            userPoints -= points;
            System.out.println("✅ Points déduits. Nouveau solde : " + userPoints);
            // Ici normalement : UPDATE en base de données
            return true;
        } else {
            System.out.println("❌ Solde insuffisant pour déduire " + points + " points.");
            return false;
        }
    }

    // Méthode pour ajouter des points
    public void ajouterPoints(int userId, int points) {
        userPoints += points;
        System.out.println("✅ Points ajoutés. Nouveau solde : " + userPoints);
        // Ici aussi normalement : UPDATE en base de données
    }
}

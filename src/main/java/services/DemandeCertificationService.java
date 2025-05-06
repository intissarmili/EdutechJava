

package services;

import java.util.HashMap;
import java.util.Map;

public class DemandeCertificationService {

    private final Map<String, Boolean> demandes = new HashMap<>();

    public void envoyerDemandeCertification(int userId, int certificationId) {
        String key = userId + "-" + certificationId;
        demandes.put(key, false); // Envoyé, mais pas encore accepté
    }

    public boolean isCertificationAcceptee(int userId, int certificationId) {
        String key = userId + "-" + certificationId;
        return demandes.getOrDefault(key, false);
    }

    // Juste pour simuler que l'admin accepte la certification
    public void accepterCertification(int userId, int certificationId) {
        String key = userId + "-" + certificationId;
        demandes.put(key, true);
    }
}

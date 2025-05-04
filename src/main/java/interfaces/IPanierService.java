package interfaces;

import models.Panier;

public interface IPanierService {
    Panier getPanierById(int id);
    void updateTotal(int id, double nouveauTotal);
}

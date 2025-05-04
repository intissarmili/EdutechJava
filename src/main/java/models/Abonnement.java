package models;

import javafx.beans.property.*;

public class Abonnement {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty nom = new SimpleStringProperty();
    private StringProperty duree = new SimpleStringProperty();
    private StringProperty prix = new SimpleStringProperty();
    private StringProperty description = new SimpleStringProperty();

    public Abonnement(int id, String nom, String duree, String prix, String description) {
        this.id.set(id);
        this.nom.set(nom);
        this.duree.set(duree);
        this.prix.set(prix);
        this.description.set(description);
    }

    public Abonnement(String nom, String duree, String prix, String description) {
        this.nom.set(nom);
        this.duree.set(duree);
        this.prix.set(prix);
        this.description.set(description);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getNom() {
        return nom.get();
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public String getDuree() {
        return duree.get();
    }

    public StringProperty dureeProperty() {
        return duree;
    }

    public String getPrix() {
        return prix.get();
    }

    public StringProperty prixProperty() {
        return prix;
    }

    public String getDescription() {
        return description.get();
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public void setDuree(String duree) {
        this.duree.set(duree);
    }

    public void setPrix(String prix) {
        this.prix.set(prix);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }
}

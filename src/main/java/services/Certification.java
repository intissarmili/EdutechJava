package services;

public class Certification {
    private int id;
    private String nom;
    private String description; // Ajouté description
    private double prix; // Prix de la certification
    private String img; // Image de la certification (URL ou chemin vers l'image)

    // Constructeur
    public Certification(int id, String nom, String description, double prix, String img) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.img = img;
    }

    public Certification(String certificationJava, double v) {

    }
    private String pdfPath;



    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}

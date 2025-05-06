package models;

public class Certification {

    private int id;
    private String nom;
    private String description;
    private double prix;  // Harmonisation du type en double pour prix
    private String img;
    private int prixPiece;
    private Integer note;
    private String pdfPath;

    // Constructeur avec ID, prix et prixPiece
    public Certification(int id, String nom, String description, double prix, String img, int prixPiece) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.img = img;
        this.prixPiece = prixPiece;
        this.pdfPath = null; // valeur par défaut
        this.note = null; // valeur par défaut
    }

    // Constructeur sans prixPiece, avec un prix en double
    public Certification(int id, String nom, String description, double prix, String img) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.img = img;
        this.prixPiece = 0; // valeur par défaut
        this.pdfPath = null; // valeur par défaut
        this.note = null; // valeur par défaut
    }

    // Constructeur vide
    public Certification() {
        this.prixPiece = 0;  // valeur par défaut
        this.pdfPath = null;
        this.note = null;
    }

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

    public int getPrixPiece() {
        return prixPiece;
    }
    public void setPrixPiece(int prixPiece) {
        this.prixPiece = prixPiece;
    }

    public Integer getNote() {
        return note;
    }
    public void setNote(Integer note) {
        this.note = note;
    }

    public String getPdfPath() {
        return pdfPath;
    }
    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    // Méthode toString() pour faciliter l'affichage et le débogage
    @Override
    public String toString() {
        return "Certification{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", img='" + img + '\'' +
                ", prixPiece=" + prixPiece +
                ", note=" + note +
                ", pdfPath='" + pdfPath + '\'' +
                '}';
    }
}

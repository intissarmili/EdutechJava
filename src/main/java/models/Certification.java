package models;

public class Certification {

    private int id;
    private String nom;
    private String description;
    private double prix;
    private String img;
    private int prixPiece;
    private Integer note;
    private String pdfPath;

    // Constructeur sans ID
    public Certification(String nom, String description, double prix, String img, int prixPiece, String pdfPath) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.img = img;
        this.prixPiece = prixPiece;
        this.pdfPath = pdfPath;
    }

    // Constructeur avec ID
    public Certification(int id, String nom, String description, double prix, String img, int prixPiece, String pdfPath) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.img = img;
        this.prixPiece = prixPiece;
        this.pdfPath = pdfPath;
    }

    // Constructeur vide
    public Certification() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public int getPrixPiece() { return prixPiece; }
    public void setPrixPiece(int prixPiece) { this.prixPiece = prixPiece; }

    public Integer getNote() { return note; }
    public void setNote(Integer note) { this.note = note; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}

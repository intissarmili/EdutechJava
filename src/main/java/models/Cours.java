package models;

public class Cours {

    private int id;
    private String titre;
    private String contenu;
    private String categorie;
    private int certificationId; // Au lieu de l'objet Certification

    // Constructeur sans ID (pour l'insertion)
    public Cours(String titre, String contenu, String categorie, int certificationId) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.certificationId = certificationId;
    }

    // Constructeur avec ID (pour la mise à jour)
    public Cours(int id, String titre, String contenu, String categorie, int certificationId) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.categorie = categorie;
        this.certificationId = certificationId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public int getCertificationId() { return certificationId; }
    public void setCertificationId(int certificationId) { this.certificationId = certificationId; }





}

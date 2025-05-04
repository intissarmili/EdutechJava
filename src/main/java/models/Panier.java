package models;

public class Panier {
    private int id;
    private int nb_total;

    public Panier() {}

    public Panier(int nb_total) {
        this.nb_total = nb_total;
    }

    public Panier(int id, int nb_total) {
        this.id = id;
        this.nb_total = nb_total;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNb_total() {
        return nb_total;
    }

    public void setNb_total(int nb_total) {
        this.nb_total = nb_total;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "id=" + id +
                ", nb_total=" + nb_total +
                '}';
    }
}

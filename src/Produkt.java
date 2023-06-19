public class Produkt {
    private int id;
    private String nazwa;
    private double cena;
    private String opis;

    public Produkt() {
        // Konstruktor domyślny
    }

    public Produkt(int id, String nazwa, double cena, String opis) {
        this.id = id;
        this.nazwa = nazwa;
        this.cena = cena;
        this.opis = opis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public double getCena() {
        return cena;
    }

    public void setCena(double cena) {
        this.cena = cena;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
}

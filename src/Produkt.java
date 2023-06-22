/**
 * Klasa Produkt reprezentuje model produktu w systemie. Posiada  id, nazwa, cena i opis produktu
 */
public class Produkt {
    private int id;
    private String nazwa, opis;
    private double cena;
    /**
     * Domyślny konstruktor klasy Produkt.
     */
    public Produkt() {}

    /**
     * Konstruktor klasy Produkt.
     *
     * @param id    identyfikator produktu
     * @param nazwa nazwa produktu
     * @param cena  cena produktu
     * @param opis  opis produktu
     */
    public Produkt(int id, String nazwa, double cena, String opis) {
        this.id = id;
        this.nazwa = nazwa;
        this.cena = cena;
        this.opis = opis;
    }
    /**
     * Metoda zwracająca identyfikator produktu.
     *
     * @return identyfikator produktu
     */
    public int getId() {
        return id;
    }
    /**
     * Metoda do ustawiania identyfikatora produktu.
     *
     * @param id identyfikator produktu
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Metoda zwracająca nazwę produktu.
     *
     * @return nazwa produktu
     */
    public String getNazwa() {
        return nazwa;
    }
    /**
     * Metoda do ustawiania nazwy produktu.
     *
     * @param nazwa nazwa produktu
     */
    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }
    /**
     * Metoda zwracająca cenę produktu.
     *
     * @return cena produktu
     */
    public double getCena() {
        return cena;
    }
    /**
     * Metoda do ustawiania ceny produktu.
     *
     * @param cena cena produktu
     */
    public void setCena(double cena) {
        this.cena = cena;
    }
    /**
     * Metoda zwracająca opis produktu.
     *
     * @return opis produktu
     */
    public String getOpis() {
        return opis;
    }
    /**
     * Metoda do ustawiania opisu produktu.
     *
     * @param opis opis produktu
     */
    public void setOpis(String opis) {
        this.opis = opis;
    }
}

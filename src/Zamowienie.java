import java.util.ArrayList;
import java.util.List;

/**
 * Klasa Zamowienie reprezentuje zamówienie w systemie.
 * Zawiera informacje takie jak numer zamówienia, klient oraz lista produktów.
 * Pozwala na dodawanie produktów do zamówienia.
 */
public class Zamowienie {
    private int numerZamowienia;
    private Klient klient;
    private List<Produkt> produkty;
    /**
     * Konstruktor klasy Zamowienie.
     * Inicjalizuje listy produktów
     * @param numerZamowienia numer zamówienia
     * @param klient klient składający zamówienie
     */
    public Zamowienie(int numerZamowienia, Klient klient) {
        this.numerZamowienia = numerZamowienia;
        this.klient = klient;
        this.produkty = new ArrayList<>();
    }
    /**
     * Metoda dodająca produkt do zamówienia.
     *
     * @param produkt produkt do dodania
     */
    public void dodajProdukt(Produkt produkt) {
        produkty.add(produkt);
    }
    /**
     * Metoda zwracająca numer zamówienia.
     *
     * @return numer zamówienia
     */
    public int getNumerZamowienia() {
        return numerZamowienia;
    }

    /**
     * Metoda ustawiająca numer zamówienia.
     *
     * @param numerZamowienia numer zamówienia
     */
    public void setNumerZamowienia(int numerZamowienia) {
        this.numerZamowienia = numerZamowienia;
    }

    /**
     * Metoda zwracająca klienta składającego zamówienie.
     *
     * @return klient składający zamówienie
     */
    public Klient getKlient() {
        return klient;
    }

    /**
     * Metoda ustawiająca klienta składającego zamówienie.
     *
     * @param klient klient składający zamówienie
     */
    public void setKlient(Klient klient) {
        this.klient = klient;
    }

    /**
     * Metoda zwracająca listę produktów w zamówieniu.
     *
     * @return lista produktów w zamówieniu
     */
    public List<Produkt> getProdukty() {
        return produkty;
    }

    /**
     * Metoda ustawiająca listę produktów w zamówieniu.
     *
     * @param produkty lista produktów w zamówieniu
     */
    public void setProdukty(List<Produkt> produkty) {
        this.produkty = produkty;
    }
}

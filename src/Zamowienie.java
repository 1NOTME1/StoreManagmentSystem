import java.util.ArrayList;
import java.util.List;

public class Zamowienie {
    private int numerZamowienia;
    private Klient klient;
    private List<Produkt> produkty;
    public Zamowienie(int numerZamowienia, Klient klient) {
        this.numerZamowienia = numerZamowienia;
        this.klient = klient;
        this.produkty = new ArrayList<>();
    }

    public void dodajProdukt(Produkt produkt) {
        produkty.add(produkt);
    }
    public int getNumerZamowienia() {
        return numerZamowienia;
    }
    public void setNumerZamowienia(int numerZamowienia) {
        this.numerZamowienia = numerZamowienia;
    }
    public Klient getKlient() {
        return klient;
    }
    public void setKlient(Klient klient) {
        this.klient = klient;
    }
    public List<Produkt> getProdukty() {
        return produkty;
    }
    public void setProdukty(List<Produkt> produkty) {
        this.produkty = produkty;
    }
}

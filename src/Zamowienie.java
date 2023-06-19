import java.util.ArrayList;
import java.util.List;

public class Zamowienie {
    private int numerZamowienia;
    private Klient klient;
    private List<Produkt> produkty;
    // dodatkowe pola i metody

    public Zamowienie(int numerZamowienia, Klient klient) {
        this.numerZamowienia = numerZamowienia;
        this.klient = klient;
        this.produkty = new ArrayList<>();
    }

    public void dodajProdukt(Produkt produkt) {
        produkty.add(produkt);
    }

    // getters i setters oraz inne metody
}

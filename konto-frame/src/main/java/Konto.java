import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Konto {
    private final String id;
    private final String inhaber;
    private BigDecimal saldo = BigDecimal.ZERO;
    private final List<Buchung> buchungen = new ArrayList<>();

    public Konto(String id, String inhaber) {
        this.id = id;
        this.inhaber = inhaber;
    }

    public void einzahlen(BigDecimal betrag) {
        if (betrag.signum() <= 0) {
            throw new IllegalArgumentException("Betrag muss positiv sein");
        }
        this.saldo = this.saldo.add(betrag);
        buchungen.add(new Buchung(betrag, "Einzahlung", LocalDateTime.now()));
    }

    public void abheben(BigDecimal betrag) throws UnzureichendesGuthabenException {
        if (saldo.compareTo(betrag) < 0) {
            throw new UnzureichendesGuthabenException("Unzureichendes Guthaben");
        }
        this.saldo = this.saldo.subtract(betrag);
        buchungen.add(new Buchung(betrag.negate(), "Auszahlung", LocalDateTime.now()));
    }

    public String getId() { return id; }
    public String getInhaber() { return inhaber; }
    public BigDecimal getSaldo() { return saldo; }
    public List<Buchung> getBuchungen() { return List.copyOf(buchungen); }
}
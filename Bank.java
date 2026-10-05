import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Bank {
    private Map<String, Konto> konten = new HashMap<>();

    public void kontoAnlegen(String inhaber, String id, BigDecimal saldo){
        Konto konto = new Konto(id, inhaber);
        konto.einzahlen(saldo);
        konten.put(id, konto);
    }
    
    public Konto kontoSuchen(String id){
        return konten.get(id);
    }

    void ueberweisung(String empfaengerID, BigDecimal empfaengerBetrag, String senderID, BigDecimal senderBetrag){
        boolean kontogedeckt = false;
        try {
            konten.get(senderID).abheben(senderBetrag);
            kontogedeckt = true;
        }
        catch(UnzureichendesGuthabenException e) {
            System.out.println("Fehler:" + e.getLocalizedMessage());
            System.out.println("Konto nicht ausreichend gedeckt");
        }
        if(kontogedeckt) {
            konten.get(empfaengerID).einzahlen(empfaengerBetrag);
        }
    }
}

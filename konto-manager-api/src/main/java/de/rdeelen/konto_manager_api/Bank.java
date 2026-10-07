package de.rdeelen.konto_manager_api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class Bank {
    private Map<String, Konto> konten = new HashMap<>();

    public Konto kontoAnlegen(String inhaber){
        //function creates a unique id and than based on that id a new Konto with 0€
        String id = UUID.randomUUID().toString();
        Konto konto = new Konto(id, inhaber);
        konten.put(id, konto);
        return konto;
    }
    
    public Konto kontoSuchen(String id){
        //function finds and returns a konto based on the unique id
        Konto konto = konten.get(id);
        if(konto == null) {
            throw new KontoNichtGefundenException(id);
        }
        return konten.get(id);
    }

    public void ueberweisung(String senderID, String empfaengerID, BigDecimal betrag) 
        //function to move money from one Konto to another
        throws UnzureichendesGuthabenException{
        Konto sender = kontoSuchen(senderID);
        Konto empfaenger = kontoSuchen(empfaengerID);
        empfaenger.einzahlen(betrag);
    }

    public Map getKonten() { return konten; }
}

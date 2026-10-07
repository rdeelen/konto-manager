package de.rdeelen.konto_manager_api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/konten")
public class KontoController {

    private final Bank bank;

    public KontoController(Bank bank) { this.bank = bank; }

    record NeuesKonto(String inhaber) {}
    record Betrag(java.math.BigDecimal betrag) {}
    record Transfer(String senderID, String empfaengerID, java.math.BigDecimal betrag) {}

    @PostMapping
    public Konto anlegen(@RequestBody NeuesKonto req) {
        return bank.kontoAnlegen(req.inhaber());
    }

    @GetMapping("/{id}")
    public Konto lesen(@PathVariable String id) {
        return bank.kontoSuchen(id);
    }

    @PostMapping("/{id}/einzahlung")
    public Konto einzahlen(@PathVariable String id, @RequestBody Betrag req) {
        Konto k = bank.kontoSuchen(id);
        k.einzahlen(req.betrag());
        return k;
    }

    @PostMapping("/konten")
    public Map kontenList(@RequestBody Map kontenMap) {
        kontenMap = bank.getKonten();
        return kontenMap;
    }

    @PostMapping("/{id}/abhebung")
    public Konto abheben(@PathVariable String id, @RequestBody Betrag req) 
        throws UnzureichendesGuthabenException{
        Konto k = bank.kontoSuchen(id);
        k.abheben(req.betrag());
        return k;
    }
    
    @PostMapping("/transfer")
    public void transfer(@RequestBody Transfer req) throws UnzureichendesGuthabenException {
        bank.ueberweisung(req.senderID, req.empfaengerID, req.betrag);
    }
    
}

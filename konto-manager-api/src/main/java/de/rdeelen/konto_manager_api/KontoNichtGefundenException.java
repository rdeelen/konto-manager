package de.rdeelen.konto_manager_api;

public class KontoNichtGefundenException extends RuntimeException {
    public KontoNichtGefundenException(String id) {
        super("Konto nicht gefunden: " + id);
    }
}
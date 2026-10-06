package de.rdeelen.konto_manager_api;

public class UnzureichendesGuthabenException extends Exception {
    public UnzureichendesGuthabenException(String message) { super(message); }
}
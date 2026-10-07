package de.rdeelen.konto_manager_api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class FehlerHandler {
    
    @ExceptionHandler(KontoNichtGefundenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String nichtGefunden(KontoNichtGefundenException e) {
        return e.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String ungueltig(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(UnzureichendesGuthabenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String zuWenigGuthaben(UnzureichendesGuthabenException e) {
        return e.getMessage();
    }
}

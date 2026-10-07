package de.rdeelen.konto_manager_api;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

   class KontoTest {
       @Test 
       void testEinzahlen() {
            Konto k = new Konto("1", "Max");
            k.einzahlen(new BigDecimal("100"));
            k.einzahlen(new BigDecimal("50"));
            assertEquals(new BigDecimal("150"), k.getSaldo());
       }

       @Test 
       void testeEinzahlenMitUngültigemBetrag() {
            Konto k = new Konto("1", "Max");
            k.einzahlen(new BigDecimal("100"));
            
            assertThrows(IllegalArgumentException.class, 
                () -> k.einzahlen(new BigDecimal("-50")));
       }

       @Test 
       void testAbheben() throws UnzureichendesGuthabenException {
            Konto k = new Konto("1", "Max");
            k.einzahlen(new BigDecimal("100"));
            k.abheben(new BigDecimal("5"));   
            assertEquals(new BigDecimal("95"), k.getSaldo());
       }

       @Test 
       void testBuchungen() {
            Konto k = new Konto("1", "Max");
            int check = 0;
            for (int i = 1; i < 6; i++) {
                k.einzahlen(new BigDecimal(Integer.toString(i)));
                check = check + i;
            }
            assertEquals(new BigDecimal(check), k.getSaldo());
       }

       @Test 
       void testÜberweisung() throws UnzureichendesGuthabenException{
            Bank b = new Bank();
            b.kontoAnlegen("Max");
            b.kontoAnlegen("Wili");
            b.ueberweisung("2", "1", new BigDecimal("50"));
            assertEquals(new BigDecimal("150"), b.kontoSuchen("1").getSaldo());
            assertEquals(new BigDecimal("150"), b.kontoSuchen("2").getSaldo());
       }

       @Test
       void abhebenMitZuWenigGuthabenWirftException() throws UnzureichendesGuthabenException {
           Konto k = new Konto("1", "Max");
           k.einzahlen(new BigDecimal("50"));
           assertThrows(UnzureichendesGuthabenException.class,
                   () -> k.abheben(new BigDecimal("80")));
       }
   }
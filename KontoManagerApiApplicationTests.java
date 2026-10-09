package de.rdeelen.konto_manager_api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
 
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class KontoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- Hilfsmethoden ----------
 
    /** Legt ein Konto über die API an und gibt dessen ID zurück. */
    private String kontoAnlegen(String inhaber) throws Exception {
        String antwort = mockMvc.perform(post("/konten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inhaber\": \"" + inhaber + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(antwort, "$.id");
    }
 
    private void einzahlen(String id, int betrag) throws Exception {
        mockMvc.perform(post("/konten/" + id + "/einzahlung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betrag\": " + betrag + "}"))
                .andExpect(status().isOk());
    }
 
    private String transferJson(String senderId, String empfaengerId, int betrag) {
        return "{\"senderID\": \"" + senderId + "\", "
                + "\"empfaengerID\": \"" + empfaengerId + "\", "
                + "\"betrag\": " + betrag + "}";
    }
 
    // ---------- Konto anlegen / lesen ----------
 
    @Test
    void kontoAnlegenGibtKontoMitInhaberZurueck() throws Exception {
        mockMvc.perform(post("/konten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inhaber\": \"Max\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.inhaber").value("Max"))
                .andExpect(jsonPath("$.saldo").value(0));
    }
 
    @Test
    void kontoLesenGibtAngelegtesKontoZurueck() throws Exception {
        String id = kontoAnlegen("Wili");
 
        mockMvc.perform(get("/konten/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.inhaber").value("Wili"));
    }
 
    @Test
    void unbekanntesKontoGibt404() throws Exception {
        mockMvc.perform(get("/konten/gibtsnicht"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Konto nicht gefunden")));
    }
 
    @Test
    void alleKontenEnthaeltAngelegtesKonto() throws Exception {
        String id = kontoAnlegen("Erika");
 
        mockMvc.perform(get("/konten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
    }
 
    // ---------- Einzahlen ----------
 
    @Test
    void einzahlenErhoehtSaldoUndLegtBuchungAn() throws Exception {
        String id = kontoAnlegen("Max");
 
        mockMvc.perform(post("/konten/" + id + "/einzahlung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betrag\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(100))
                .andExpect(jsonPath("$.buchungen", hasSize(1)));
    }
 
    @Test
    void einzahlenMitNegativemBetragGibt400() throws Exception {
        String id = kontoAnlegen("Max");
 
        mockMvc.perform(post("/konten/" + id + "/einzahlung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betrag\": -50}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Betrag muss positiv sein")));
    }
 
    @Test
    void einzahlenAufUnbekanntesKontoGibt404() throws Exception {
        mockMvc.perform(post("/konten/gibtsnicht/einzahlung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betrag\": 10}"))
                .andExpect(status().isNotFound());
    }
 
    // ---------- Abheben ----------
 
    @Test
    void abhebenVerringertSaldo() throws Exception {
        String id = kontoAnlegen("Max");
        einzahlen(id, 100);
 
        mockMvc.perform(post("/konten/" + id + "/abhebung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betrag\": 30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(70))
                .andExpect(jsonPath("$.buchungen", hasSize(2)));
    }
 
    @Test
    void abhebenMitZuWenigGuthabenGibt409() throws Exception {
        String id = kontoAnlegen("Max");
        einzahlen(id, 50);
 
        mockMvc.perform(post("/konten/" + id + "/abhebung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"betrag\": 80}"))
                .andExpect(status().isConflict());
 
        // Saldo muss unverändert sein
        mockMvc.perform(get("/konten/" + id))
                .andExpect(jsonPath("$.saldo").value(50));
    }
 
    // ---------- Transfer ----------
 
    @Test
    void transferBuchtGeldVonSenderZuEmpfaenger() throws Exception {
        String sender = kontoAnlegen("Max");
        String empfaenger = kontoAnlegen("Wili");
        einzahlen(sender, 100);
 
        mockMvc.perform(post("/konten/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(sender, empfaenger, 40)))
                .andExpect(status().isOk());
 
        mockMvc.perform(get("/konten/" + sender))
                .andExpect(jsonPath("$.saldo").value(60));
        mockMvc.perform(get("/konten/" + empfaenger))
                .andExpect(jsonPath("$.saldo").value(40));
    }
 
    @Test
    void transferMitZuWenigGuthabenGibt409UndAendertNichts() throws Exception {
        String sender = kontoAnlegen("Max");
        String empfaenger = kontoAnlegen("Wili");
        einzahlen(sender, 10);
 
        mockMvc.perform(post("/konten/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(sender, empfaenger, 50)))
                .andExpect(status().isConflict());
 
        mockMvc.perform(get("/konten/" + sender))
                .andExpect(jsonPath("$.saldo").value(10));
        mockMvc.perform(get("/konten/" + empfaenger))
                .andExpect(jsonPath("$.saldo").value(0));
    }
 
    @Test
    void transferAnUnbekanntesKontoGibt404() throws Exception {
        String sender = kontoAnlegen("Max");
        einzahlen(sender, 100);
 
        mockMvc.perform(post("/konten/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(sender, "gibtsnicht", 10)))
                .andExpect(status().isNotFound());
 
        mockMvc.perform(get("/konten/" + sender))
                .andExpect(jsonPath("$.saldo").value(100));
    }
}

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Buchung(BigDecimal betrag, String text, LocalDateTime zeitpunkt) {}
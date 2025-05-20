package com.example.app.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ExchangeRateService {

    // Ratele sunt definite relativ la o monedă de bază sau direct între perechi.
    // Exemplu: Cât valorează 1 unitate din 'fromCurrency' în 'toCurrency'.
    // Pentru simplitate, vom defini ratele față de LEI.
    // 1 USD = X LEI, 1 EUR = Y LEI
    // Pentru a converti USD în EUR: USD -> LEI -> EUR
    private static final Map<String, BigDecimal> ratesToLEI = new HashMap<>();

    static {
        // Acestea sunt rate de CUMPĂRARE pentru bancă (când banca cumpără valută de la client)
        // sau rate de VÂNZARE pentru client (când clientul cumpără valută de la bancă)
        // Pentru transferuri, de obicei se folosește o rată medie sau o rată specifică tranzacției.
        // Să presupunem că acestea sunt ratele la care se face conversia pentru client.
        // 1 USD = 17.50 LEI
        // 1 EUR = 19.00 LEI
        // 1 LEI = 1.00 LEI (pentru coerență)
        ratesToLEI.put("USD", new BigDecimal("17.50"));
        ratesToLEI.put("EUR", new BigDecimal("19.00"));
        ratesToLEI.put("LEI", BigDecimal.ONE);
    }

    /**
     * Convertește o sumă dintr-o monedă în alta.
     * @param amount Suma de convertit.
     * @param fromCurrency Moneda sursă (ex: "USD").
     * @param toCurrency Moneda destinație (ex: "LEI").
     * @return Suma convertită.
     * @throws IllegalArgumentException dacă monedele nu sunt suportate.
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        Objects.requireNonNull(amount, "Suma nu poate fi nulă");
        Objects.requireNonNull(fromCurrency, "Moneda sursă nu poate fi nulă");
        Objects.requireNonNull(toCurrency, "Moneda destinație nu poate fi nulă");

        String fromUpper = fromCurrency.toUpperCase();
        String toUpper = toCurrency.toUpperCase();

        if (fromUpper.equals(toUpper)) {
            return amount; // Nu e necesară conversia
        }

        BigDecimal rateFrom = ratesToLEI.get(fromUpper);
        BigDecimal rateTo = ratesToLEI.get(toUpper);

        if (rateFrom == null) {
            throw new IllegalArgumentException("Moneda sursă nesuportată pentru conversie: " + fromCurrency);
        }
        if (rateTo == null) {
            throw new IllegalArgumentException("Moneda destinație nesuportată pentru conversie: " + toCurrency);
        }

        // Conversie: Suma în LEI = amount * rateFrom (dacă rateFrom este rata X Valută = 1 LEI)
        // Sau, dacă ratele noastre sunt 1 VALUTĂ_STRĂINĂ = X LEI:
        // amount_in_FROM_CURRENCY * (LEI / FROM_CURRENCY) / (LEI / TO_CURRENCY)
        // = amount_in_FROM_CURRENCY * rate_FROM_to_LEI / rate_TO_to_LEI (dacă rateTo e 1 TO_CURRENCY = Z LEI)
        // = amount_in_FROM_CURRENCY * rate_FROM_to_LEI * (1 / rate_TO_to_LEI)

        // Exemplu: 100 USD în EUR
        // 100 USD * (17.50 LEI / 1 USD) = 1750 LEI
        // 1750 LEI / (19.00 LEI / 1 EUR) = 1750 LEI * (1 EUR / 19.00 LEI) = 92.10 EUR

        BigDecimal amountInLEI = amount.multiply(rateFrom); // Convertește suma din fromCurrency în LEI
        BigDecimal convertedAmount = amountInLEI.divide(rateTo, 2, RoundingMode.HALF_UP); // Convertește din LEI în toCurrency

        System.out.printf("[ExchangeRateService] Converting %.2f %s to %s: %.2f %s (Rate LEI/%s=%.4f, Rate LEI/%s=%.4f)%n",
                amount, fromUpper, toUpper, convertedAmount, toUpper, fromUpper, rateFrom, toUpper, rateTo);

        return convertedAmount;
    }

    /**
     * Obține rata de schimb pentru a converti 1 unitate din fromCurrency în toCurrency.
     * @param fromCurrency Moneda sursă.
     * @param toCurrency Moneda destinație.
     * @return Rata de schimb.
     */
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        Objects.requireNonNull(fromCurrency, "Moneda sursă nu poate fi nulă");
        Objects.requireNonNull(toCurrency, "Moneda destinație nu poate fi nulă");

        String fromUpper = fromCurrency.toUpperCase();
        String toUpper = toCurrency.toUpperCase();

        if (fromUpper.equals(toUpper)) {
            return BigDecimal.ONE;
        }

        BigDecimal rateFrom = ratesToLEI.get(fromUpper);
        BigDecimal rateTo = ratesToLEI.get(toUpper);

        if (rateFrom == null || rateTo == null) {
            throw new IllegalArgumentException("Una dintre monede nu este suportată pentru obținerea ratei de schimb.");
        }
        // Rata pentru 1 fromCurrency = X toCurrency este (rateFrom / rateTo)
        return rateFrom.divide(rateTo, 4, RoundingMode.HALF_UP); // 4 zecimale pentru rate
    }
}
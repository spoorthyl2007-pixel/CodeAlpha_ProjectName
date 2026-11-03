package stocktrading;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single stock trade — either a BUY or a SELL.
 *
 * Every time a user buys or sells shares, one Transaction object is created
 * and stored in their Portfolio's transaction history.
 *
 * A Transaction is immutable: once created, none of its values can change.
 * This preserves an accurate audit trail of every trade.
 */
public class Transaction {

    // -------------------------------------------------------------------------
    // Fields  (all final — a completed trade record never changes)
    // -------------------------------------------------------------------------

    /** Whether this was a BUY or a SELL. */
    private final TransactionType type;

    /** The ticker symbol of the traded stock, e.g. "AAPL". */
    private final String stockSymbol;

    /** Number of shares bought or sold. */
    private final int quantity;

    /** Price per share at the moment of the trade. */
    private final double price;

    /** Exact date and time the trade was executed. Set automatically. */
    private final LocalDateTime timestamp;

    // Formatter used in toString() — e.g. "2026-11-03 10:45:30"
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Transaction record and stamps it with the current time.
     *
     * @param type        BUY or SELL — must not be null
     * @param stockSymbol ticker symbol — must not be null or blank
     * @param quantity    number of shares — must be >= 1
     * @param price       price per share  — must be >= 0
     */
    public Transaction(TransactionType type, String stockSymbol,
                       int quantity, double price) {

        // --- Validate type ---
        if (type == null) {
            throw new IllegalArgumentException(
                "Transaction type cannot be null. Use TransactionType.BUY or SELL.");
        }

        // --- Validate stock symbol ---
        if (stockSymbol == null || stockSymbol.isBlank()) {
            throw new IllegalArgumentException(
                "Stock symbol cannot be empty.");
        }

        // --- Validate quantity ---
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be at least 1. Received: " + quantity);
        }

        // --- Validate price ---
        if (price < 0) {
            throw new IllegalArgumentException(
                "Price cannot be negative. Received: " + price);
        }

        // Store validated values
        this.type        = type;
        this.stockSymbol = stockSymbol.toUpperCase().trim();
        this.quantity    = quantity;
        this.price       = price;

        // Automatically capture the exact moment of this trade
        this.timestamp   = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Getters  (no setters — Transaction is intentionally immutable)
    // -------------------------------------------------------------------------

    /**
     * Returns the type of this transaction: BUY or SELL.
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Returns the ticker symbol of the stock that was traded (e.g. "TSLA").
     */
    public String getStockSymbol() {
        return stockSymbol;
    }

    /**
     * Returns the number of shares that were bought or sold.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the price per share at which the trade was executed.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the date and time when this transaction was created.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // -------------------------------------------------------------------------
    // Convenience method
    // -------------------------------------------------------------------------

    /**
     * Calculates and returns the total monetary value of this transaction.
     *
     * Formula: quantity × price per share
     *
     * Example: buying 10 shares at $150.00 = $1,500.00
     */
    public double getTotalValue() {
        return quantity * price;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    /**
     * Returns a clear, single-line summary of this transaction.
     *
     * Example BUY output:
     *   [2026-11-03 10:45:30] BUY  10 shares of AAPL @ $189.75 | Total: $1,897.50
     *
     * Example SELL output:
     *   [2026-11-03 11:02:15] SELL  5 shares of TSLA @ $245.00 | Total: $1,225.00
     */
    @Override
    public String toString() {
        return String.format(
            "[%s] %-4s %4d shares of %-6s @ $%8.2f | Total: $%,.2f",
            timestamp.format(DISPLAY_FORMAT),  // formatted date-time
            type,                               // BUY or SELL
            quantity,                           // number of shares
            stockSymbol,                        // ticker
            price,                              // price per share
            getTotalValue()                     // quantity * price
        );
    }
}

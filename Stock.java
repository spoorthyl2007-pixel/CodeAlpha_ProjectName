package stocktrading;

/**
 * Represents a single stock listed on the simulated market.
 *
 * Each stock has three pieces of data:
 *   - symbol      : a short code for the stock, e.g. "AAPL"
 *   - companyName : the full company name, e.g. "Apple Inc."
 *   - price       : the current price per share in USD
 */
public class Stock {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    // 'final' means symbol and companyName can never change after the object
    // is created — a stock's identity stays fixed.
    private final String symbol;
    private final String companyName;

    // Price is NOT final because it can change as the market moves.
    private double price;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Stock with the given details.
     *
     * @param symbol      ticker symbol (e.g. "GOOG") — must not be null or blank
     * @param companyName full company name  — must not be null or blank
     * @param price       initial price per share — must be >= 0
     */
    public Stock(String symbol, String companyName, double price) {
        // Validate symbol
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol cannot be empty.");
        }
        // Validate company name
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        // Validate price — a stock price can be 0 but never negative
        if (price < 0) {
            throw new IllegalArgumentException(
                "Stock price cannot be negative. Received: " + price);
        }

        // Store the values. toUpperCase() keeps symbols consistent ("aapl" -> "AAPL").
        this.symbol      = symbol.toUpperCase().trim();
        this.companyName = companyName.trim();
        this.price       = price;
    }

    // -------------------------------------------------------------------------
    // Getters  (read-only access to the fields)
    // -------------------------------------------------------------------------

    /**
     * Returns the ticker symbol of this stock (e.g. "AAPL").
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the full company name (e.g. "Apple Inc.").
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Returns the current price per share in USD.
     */
    public double getPrice() {
        return price;
    }

    // -------------------------------------------------------------------------
    // Setter  (only price can change)
    // -------------------------------------------------------------------------

    /**
     * Updates the current price of this stock.
     * Rejects negative values to keep data consistent.
     *
     * @param newPrice the new price per share — must be >= 0
     */
    public void setPrice(double newPrice) {
        if (newPrice < 0) {
            throw new IllegalArgumentException(
                "Stock price cannot be negative. Received: " + newPrice);
        }
        this.price = newPrice;
    }

    // -------------------------------------------------------------------------
    // toString  (human-readable display)
    // -------------------------------------------------------------------------

    /**
     * Returns a neatly formatted single-line summary of this stock.
     *
     * Example output:
     *   AAPL   | Apple Inc.               | $189.75
     */
    @Override
    public String toString() {
        return String.format("%-6s | %-30s | $%,.2f",
                symbol, companyName, price);
    }
}

package stocktrading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulates the stock market for the trading platform.
 *
 * Responsibilities:
 *   1. Maintain a catalogue of all tradeable stocks.
 *   2. Display current market prices.
 *   3. Execute BUY and SELL orders, updating the user's cash and portfolio.
 *
 * Transaction recording is intentionally delegated to Portfolio.addShares()
 * and Portfolio.removeShares() — this class does NOT create Transaction objects
 * directly, which avoids double-recording.
 */
public class StockMarket {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /**
     * Maps a stock symbol (e.g. "AAPL") to its Stock object.
     * Using a HashMap gives O(1) look-up when a user types a symbol.
     */
    private final Map<String, Stock> stocks;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Initialises the market and pre-loads 5 sample stocks.
     * These serve as the default tradeable securities on the platform.
     */
    public StockMarket() {
        this.stocks = new HashMap<>();
        loadSampleStocks();
    }

    /**
     * Adds the 5 required sample stocks to the market catalogue.
     * Prices are representative values — they will not change unless
     * updateStockPrice() is called later.
     */
    private void loadSampleStocks() {
        addStock(new Stock("AAPL",  "Apple Inc.",             189.75));
        addStock(new Stock("GOOGL", "Alphabet Inc.",         2_750.00));
        addStock(new Stock("MSFT",  "Microsoft Corporation",  415.50));
        addStock(new Stock("AMZN",  "Amazon.com Inc.",        185.30));
        addStock(new Stock("TSLA",  "Tesla Inc.",             245.00));
    }

    // -------------------------------------------------------------------------
    // Market catalogue methods
    // -------------------------------------------------------------------------

    /**
     * Adds a stock to the market.
     * If a stock with the same symbol already exists it is replaced.
     *
     * @param stock the Stock to list — must not be null
     */
    public void addStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Cannot add a null stock to the market.");
        }
        stocks.put(stock.getSymbol(), stock);
    }

    /**
     * Finds and returns a Stock by its ticker symbol.
     * The lookup is case-insensitive ("aapl" and "AAPL" both work).
     *
     * @param symbol the ticker symbol to look up
     * @return the matching Stock, or null if not listed
     */
    public Stock getStock(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        return stocks.get(symbol.toUpperCase().trim());
    }

    /**
     * Returns an unmodifiable view of all stocks currently listed.
     * Sorted alphabetically by symbol for consistent display order.
     */
    public Collection<Stock> getAllStocks() {
        // Sort by symbol so the display is always in the same order
        List<Stock> sorted = new ArrayList<>(stocks.values());
        sorted.sort((a, b) -> a.getSymbol().compareTo(b.getSymbol()));
        return Collections.unmodifiableList(sorted);
    }

    /**
     * Updates the price of an existing stock (e.g. to simulate market movement).
     * Silently ignores the request if the symbol is not listed.
     *
     * @param symbol   ticker symbol
     * @param newPrice new price — must be >= 0
     */
    public void updateStockPrice(String symbol, double newPrice) {
        Stock stock = getStock(symbol);
        if (stock != null) {
            stock.setPrice(newPrice);   // validation is inside Stock.setPrice()
        }
    }

    // -------------------------------------------------------------------------
    // displayMarket()
    // -------------------------------------------------------------------------

    /**
     * Prints a formatted table of all listed stocks to the console.
     *
     * Example output:
     * ============================================================
     *  STOCK MARKET — Available Stocks
     * ============================================================
     *  #   Symbol   Company                          Price
     * ------------------------------------------------------------
     *  1   AAPL     Apple Inc.                       $   189.75
     *  2   AMZN     Amazon.com Inc.                  $   185.30
     *  ...
     * ============================================================
     */
    public void displayMarket() {
        System.out.println("============================================================");
        System.out.println(" STOCK MARKET — Available Stocks");
        System.out.println("============================================================");
        System.out.printf(" %-3s %-6s %-32s %s%n", "#", "Symbol", "Company", "Price");
        System.out.println("------------------------------------------------------------");

        int index = 1;
        for (Stock stock : getAllStocks()) {
            System.out.printf(" %-3d %-6s %-32s $%,.2f%n",
                index++,
                stock.getSymbol(),
                stock.getCompanyName(),
                stock.getPrice());
        }

        System.out.println("============================================================");
    }

    // -------------------------------------------------------------------------
    // buyStock()
    // -------------------------------------------------------------------------

    /**
     * Executes a BUY order on behalf of a user.
     *
     * Steps (all validations happen BEFORE any state is changed):
     *   1. Validate user and quantity inputs.
     *   2. Look up the stock — reject if not listed.
     *   3. Calculate total cost.
     *   4. Attempt to deduct cash — reject if insufficient funds.
     *   5. Add shares to the portfolio (Portfolio also records the transaction).
     *   6. Print a success message.
     *
     * If any step fails, the method prints a message and returns false.
     * No partial changes are made — cash is only deducted after every check passes.
     *
     * @param user     the user placing the order — must not be null
     * @param symbol   ticker symbol of the stock to buy
     * @param quantity number of shares to purchase — must be >= 1
     * @return true if the purchase succeeded, false otherwise
     */
    public boolean buyStock(User user, String symbol, int quantity) {

        // --- Step 1: Validate inputs ---
        if (user == null) {
            System.out.println("[ERROR] No user provided for buy order.");
            return false;
        }
        if (symbol == null || symbol.isBlank()) {
            System.out.println("[ERROR] Stock symbol cannot be empty.");
            return false;
        }
        if (quantity <= 0) {
            System.out.println("[ERROR] Quantity must be at least 1. Received: " + quantity);
            return false;
        }

        // --- Step 2: Find the stock ---
        Stock stock = getStock(symbol);
        if (stock == null) {
            System.out.printf("[ERROR] Stock '%s' is not listed on this market.%n",
                symbol.toUpperCase());
            return false;
        }

        // --- Step 3: Calculate cost ---
        double totalCost = stock.getPrice() * quantity;

        // --- Step 4: Check and deduct cash (atomic — returns false if insufficient) ---
        boolean paymentSucceeded = user.deductCash(totalCost);
        if (!paymentSucceeded) {
            System.out.printf(
                "[FAILED] Insufficient funds.%n" +
                "         Required : $%,.2f%n" +
                "         Available: $%,.2f%n",
                totalCost, user.getCashBalance());
            return false;
        }

        // --- Step 5: Add shares to portfolio (Portfolio records the BUY transaction) ---
        user.getPortfolio().addShares(stock.getSymbol(), quantity, stock.getPrice());

        // --- Step 6: Success message ---
        System.out.printf(
            "[BUY SUCCESS] Bought %d share(s) of %s (%s) @ $%,.2f each.%n" +
            "              Total spent   : $%,.2f%n" +
            "              Cash remaining: $%,.2f%n",
            quantity,
            stock.getSymbol(),
            stock.getCompanyName(),
            stock.getPrice(),
            totalCost,
            user.getCashBalance());

        return true;
    }

    // -------------------------------------------------------------------------
    // sellStock()
    // -------------------------------------------------------------------------

    /**
     * Executes a SELL order on behalf of a user.
     *
     * Steps (all validations happen BEFORE any state is changed):
     *   1. Validate user and quantity inputs.
     *   2. Look up the stock — reject if not listed.
     *   3. Check the user owns enough shares.
     *   4. Remove shares from the portfolio (Portfolio also records the transaction).
     *   5. Add sale proceeds to the user's cash balance.
     *   6. Print a success message.
     *
     * If any step fails, the method prints a message and returns false.
     * Portfolio.removeShares() returns false without touching anything if
     * the user does not have enough shares — so no partial updates occur.
     *
     * @param user     the user placing the order — must not be null
     * @param symbol   ticker symbol of the stock to sell
     * @param quantity number of shares to sell — must be >= 1
     * @return true if the sale succeeded, false otherwise
     */
    public boolean sellStock(User user, String symbol, int quantity) {

        // --- Step 1: Validate inputs ---
        if (user == null) {
            System.out.println("[ERROR] No user provided for sell order.");
            return false;
        }
        if (symbol == null || symbol.isBlank()) {
            System.out.println("[ERROR] Stock symbol cannot be empty.");
            return false;
        }
        if (quantity <= 0) {
            System.out.println("[ERROR] Quantity must be at least 1. Received: " + quantity);
            return false;
        }

        // --- Step 2: Find the stock ---
        Stock stock = getStock(symbol);
        if (stock == null) {
            System.out.printf("[ERROR] Stock '%s' is not listed on this market.%n",
                symbol.toUpperCase());
            return false;
        }

        // --- Step 3: Check ownership before touching anything ---
        int sharesOwned = user.getPortfolio().getSharesOwned(stock.getSymbol());
        if (sharesOwned < quantity) {
            System.out.printf(
                "[FAILED] Not enough shares to sell.%n" +
                "         You own     : %d share(s) of %s%n" +
                "         You want to sell: %d share(s)%n",
                sharesOwned, stock.getSymbol(), quantity);
            return false;
        }

        // --- Step 4: Remove shares (Portfolio records the SELL transaction) ---
        double totalEarned = stock.getPrice() * quantity;
        boolean removeSucceeded = user.getPortfolio()
                                      .removeShares(stock.getSymbol(), quantity, stock.getPrice());

        // removeShares() should always succeed here because we checked sharesOwned above,
        // but we guard anyway to be safe.
        if (!removeSucceeded) {
            System.out.println("[ERROR] Unexpected error while removing shares. Trade cancelled.");
            return false;
        }

        // --- Step 5: Credit the user's cash balance ---
        user.addCash(totalEarned);

        // --- Step 6: Success message ---
        System.out.printf(
            "[SELL SUCCESS] Sold %d share(s) of %s (%s) @ $%,.2f each.%n" +
            "               Total earned  : $%,.2f%n" +
            "               Cash balance  : $%,.2f%n",
            quantity,
            stock.getSymbol(),
            stock.getCompanyName(),
            stock.getPrice(),
            totalEarned,
            user.getCashBalance());

        return true;
    }
}

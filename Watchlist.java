package stocktrading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A personal watchlist that lets a user track stocks they are interested in
 * without actually owning them.
 *
 * Design choices:
 *   - Uses a LinkedHashSet<String> to store symbols.
 *       * Set      → automatically prevents duplicates.
 *       * Linked   → preserves insertion order so the list stays stable.
 *   - Completely independent of Portfolio and Transaction — watching a stock
 *     never affects cash balance or trade history.
 *   - Symbol look-ups are case-insensitive ("aapl" == "AAPL").
 */
public class Watchlist {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /**
     * Stores the ticker symbols of watched stocks.
     * LinkedHashSet gives O(1) add/remove/contains AND preserves insertion order.
     */
    private final Set<String> watchedSymbols;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /** Creates a new, empty watchlist. */
    public Watchlist() {
        this.watchedSymbols = new LinkedHashSet<>();
    }

    // -------------------------------------------------------------------------
    // addStock()
    // -------------------------------------------------------------------------

    /**
     * Adds a stock symbol to the watchlist after validating it against the market.
     *
     * Outcomes:
     *   - Symbol not on the market  → prints an error, returns false.
     *   - Symbol already watched    → prints a friendly notice, returns false.
     *   - Symbol valid and new      → adds it, prints confirmation, returns true.
     *
     * @param symbol the ticker to watch (case-insensitive)
     * @param market used to verify the symbol is a real listed stock
     * @return true if the stock was newly added, false otherwise
     */
    public boolean addStock(String symbol, StockMarket market) {
        // Validate input
        if (symbol == null || symbol.isBlank()) {
            System.out.println("[ERROR] Stock symbol cannot be empty.");
            return false;
        }

        String upper = symbol.toUpperCase().trim();

        // Validate against the market catalogue
        Stock stock = market.getStock(upper);
        if (stock == null) {
            System.out.printf("[ERROR] '%s' is not listed on this market. " +
                "Use 'View Stock Market' to see available symbols.%n", upper);
            return false;
        }

        // Prevent duplicates — Set.add() returns false if already present
        boolean added = watchedSymbols.add(upper);
        if (!added) {
            System.out.printf("[NOTICE] %s (%s) is already in your watchlist.%n",
                upper, stock.getCompanyName());
            return false;
        }

        System.out.printf("[WATCHLIST] Added %s (%s) to your watchlist.%n",
            upper, stock.getCompanyName());
        return true;
    }

    // -------------------------------------------------------------------------
    // removeStock()
    // -------------------------------------------------------------------------

    /**
     * Removes a stock symbol from the watchlist.
     *
     * Outcomes:
     *   - Symbol not currently watched → prints a friendly notice, returns false.
     *   - Symbol found                 → removes it, prints confirmation, returns true.
     *
     * Note: We do NOT validate the symbol against the market here.
     * If a stock was removed from the market after being added to the watchlist,
     * the user should still be able to clean it out.
     *
     * @param symbol the ticker to remove (case-insensitive)
     * @return true if the stock was removed, false if it wasn't in the list
     */
    public boolean removeStock(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            System.out.println("[ERROR] Stock symbol cannot be empty.");
            return false;
        }

        String upper = symbol.toUpperCase().trim();

        boolean removed = watchedSymbols.remove(upper);
        if (!removed) {
            System.out.printf("[NOTICE] %s is not in your watchlist.%n", upper);
            return false;
        }

        System.out.printf("[WATCHLIST] Removed %s from your watchlist.%n", upper);
        return true;
    }

    // -------------------------------------------------------------------------
    // isWatched()
    // -------------------------------------------------------------------------

    /**
     * Returns true if the given symbol is currently in the watchlist.
     *
     * @param symbol ticker to check (case-insensitive)
     */
    public boolean isWatched(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return false;
        }
        return watchedSymbols.contains(symbol.toUpperCase().trim());
    }

    // -------------------------------------------------------------------------
    // getWatchedSymbols()
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable, insertion-ordered list of all watched symbols.
     * Useful for iteration without exposing the internal Set.
     */
    public List<String> getWatchedSymbols() {
        return Collections.unmodifiableList(new ArrayList<>(watchedSymbols));
    }

    // -------------------------------------------------------------------------
    // size() / isEmpty()
    // -------------------------------------------------------------------------

    /** Returns the number of stocks currently on the watchlist. */
    public int size() {
        return watchedSymbols.size();
    }

    /** Returns true if the watchlist has no entries. */
    public boolean isEmpty() {
        return watchedSymbols.isEmpty();
    }

    // -------------------------------------------------------------------------
    // displayWatchlist()
    // -------------------------------------------------------------------------

    /**
     * Prints a formatted table of all watched stocks with their current prices.
     *
     * Example output (populated):
     * ================================================================
     *  YOUR WATCHLIST
     * ================================================================
     *  #   Symbol  Company                          Current Price
     * ----------------------------------------------------------------
     *  1   AAPL    Apple Inc.                       $189.75
     *  2   TSLA    Tesla Inc.                       $245.00
     * ================================================================
     *
     * Example output (empty):
     * ================================================================
     *  YOUR WATCHLIST
     * ================================================================
     *  Your watchlist is empty.
     *  Use option 7 to add stocks you want to keep an eye on.
     * ================================================================
     *
     * @param market used to fetch the current price for each symbol
     */
    public void displayWatchlist(StockMarket market) {
        System.out.println("================================================================");
        System.out.println(" YOUR WATCHLIST");
        System.out.println("================================================================");

        if (watchedSymbols.isEmpty()) {
            System.out.println(" Your watchlist is empty.");
            System.out.println(" Use the 'Add to Watchlist' option to track stocks.");
            System.out.println("================================================================");
            return;
        }

        // Column headers
        System.out.printf(" %-3s %-6s %-32s %s%n",
            "#", "Symbol", "Company", "Current Price");
        System.out.println("----------------------------------------------------------------");

        int index = 1;
        for (String symbol : watchedSymbols) {
            Stock stock = market.getStock(symbol);

            if (stock == null) {
                // Stock was delisted after being added to the watchlist
                System.out.printf(" %-3d %-6s %-32s %s%n",
                    index++, symbol, "(delisted)", "N/A");
            } else {
                System.out.printf(" %-3d %-6s %-32s $%,.2f%n",
                    index++,
                    stock.getSymbol(),
                    stock.getCompanyName(),
                    stock.getPrice());
            }
        }

        System.out.println("================================================================");
    }
}

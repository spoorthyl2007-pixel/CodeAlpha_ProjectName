package stocktrading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks everything a user owns and has traded.
 *
 * Two data structures power this class:
 *   1. holdings         – a HashMap  : symbol  -> shares owned
 *   2. transactionHistory – an ArrayList : ordered list of every buy/sell
 */
public class Portfolio {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /**
     * Maps a stock symbol (e.g. "AAPL") to the number of shares the user owns.
     * When a stock's share count drops to zero it is removed from the map.
     */
    private final Map<String, Integer> holdings;

    /**
     * Chronological list of every Transaction (buy or sell) made by the user.
     */
    private final List<Transaction> transactionHistory;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a brand-new, empty portfolio.
     * Called automatically when a User object is created.
     */
    public Portfolio() {
        this.holdings          = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable view of the current holdings map.
     * Callers can read the map but cannot change it directly —
     * they must go through addShares() / removeShares().
     */
    public Map<String, Integer> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    /**
     * Returns an unmodifiable view of the full transaction history list.
     */
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    /**
     * Returns the number of shares the user owns for the given symbol.
     * Returns 0 if the stock is not in the portfolio.
     *
     * @param symbol stock ticker, e.g. "TSLA"
     */
    public int getSharesOwned(String symbol) {
        // getOrDefault returns the mapped value, or 0 if the key is absent
        return holdings.getOrDefault(symbol.toUpperCase(), 0);
    }

    // -------------------------------------------------------------------------
    // addTransaction()
    // -------------------------------------------------------------------------

    /**
     * Appends a Transaction to the history list directly.
     * Used by StockMarket after it has already validated the trade.
     *
     * @param transaction the completed transaction to record
     */
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        transactionHistory.add(transaction);
    }

    // -------------------------------------------------------------------------
    // addShares()
    // -------------------------------------------------------------------------

    /**
     * Adds shares of a stock to the portfolio AND records a BUY transaction.
     *
     * Steps:
     *  1. Validate inputs.
     *  2. Increase the share count in the holdings map.
     *  3. Append a BUY Transaction to the history.
     *
     * @param symbol        ticker symbol (e.g. "AAPL")
     * @param quantity      number of shares to add — must be >= 1
     * @param pricePerShare the price paid per share (used to record the trade)
     */
    public void addShares(String symbol, int quantity, double pricePerShare) {
        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be at least 1. Received: " + quantity);
        }
        // Validate price
        if (pricePerShare < 0) {
            throw new IllegalArgumentException(
                "Price per share cannot be negative. Received: " + pricePerShare);
        }

        String upperSymbol = symbol.toUpperCase();

        // merge() adds 'quantity' to the existing value, or stores it if absent.
        // Integer::sum is the same as (existing, newValue) -> existing + newValue
        holdings.merge(upperSymbol, quantity, Integer::sum);

        // Record the transaction
        transactionHistory.add(
            new Transaction(TransactionType.BUY, upperSymbol, quantity, pricePerShare));
    }

    // -------------------------------------------------------------------------
    // removeShares()
    // -------------------------------------------------------------------------

    /**
     * Removes shares of a stock from the portfolio AND records a SELL transaction.
     *
     * Steps:
     *  1. Validate inputs.
     *  2. Check the user actually owns enough shares.
     *  3. Decrease the share count (or remove the entry if it hits 0).
     *  4. Append a SELL Transaction to the history.
     *
     * Returns true on success, false if the user does not own enough shares.
     * Returning false (instead of throwing) lets the caller show a friendly
     * "not enough shares" message without crashing the app.
     *
     * @param symbol        ticker symbol
     * @param quantity      number of shares to remove — must be >= 1
     * @param pricePerShare the price received per share (used to record the trade)
     * @return true if the sell succeeded, false if insufficient shares
     */
    public boolean removeShares(String symbol, int quantity, double pricePerShare) {
        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be at least 1. Received: " + quantity);
        }
        // Validate price
        if (pricePerShare < 0) {
            throw new IllegalArgumentException(
                "Price per share cannot be negative. Received: " + pricePerShare);
        }

        String upperSymbol = symbol.toUpperCase();
        int currentlyOwned = getSharesOwned(upperSymbol);

        // Cannot sell more than you own
        if (quantity > currentlyOwned) {
            return false;   // caller will display a "not enough shares" message
        }

        int remaining = currentlyOwned - quantity;

        if (remaining == 0) {
            // No shares left — remove the stock from holdings entirely
            holdings.remove(upperSymbol);
        } else {
            // Update with the reduced count
            holdings.put(upperSymbol, remaining);
        }

        // Record the transaction
        transactionHistory.add(
            new Transaction(TransactionType.SELL, upperSymbol, quantity, pricePerShare));

        return true;
    }

    // -------------------------------------------------------------------------
    // displayPortfolio()
    // -------------------------------------------------------------------------

    /**
     * Prints a simple list of the user's current holdings (symbol + share count).
     * Does not require market prices — used as a quick internal check.
     *
     * Example output:
     * ----------------------------------------
     *  Current Holdings
     * ----------------------------------------
     *  AAPL   :    8 shares
     * ----------------------------------------
     */
    public void displayPortfolio() {
        System.out.println("----------------------------------------");
        System.out.println(" Current Holdings");
        System.out.println("----------------------------------------");

        if (holdings.isEmpty()) {
            System.out.println(" Your portfolio is empty. Start buying stocks!");
        } else {
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                System.out.printf(" %-6s : %4d shares%n",
                    entry.getKey(), entry.getValue());
            }
        }

        System.out.println("----------------------------------------");
    }

    // -------------------------------------------------------------------------
    // displayPortfolioSummary()
    // -------------------------------------------------------------------------

    /**
     * Prints a full portfolio summary including current prices and values.
     *
     * This method needs the StockMarket to look up live prices, and the User
     * to read the cash balance — neither is stored inside Portfolio itself.
     *
     * Example output:
     * ================================================================
     *  PORTFOLIO SUMMARY — Alice
     * ================================================================
     *  Symbol  Shares   Price/Share    Holding Value
     * ----------------------------------------------------------------
     *  AAPL         8      $189.75        $1,518.00
     *  TSLA         3      $245.00          $735.00
     * ----------------------------------------------------------------
     *  Total Stock Value :              $2,253.00
     *  Cash Balance      :              $7,747.00
     *  Overall Portfolio :              $10,000.00
     * ================================================================
     *
     * @param user   the owner of this portfolio (provides cash balance + username)
     * @param market used to look up the current price of each held stock
     */
    public void displayPortfolioSummary(User user, StockMarket market) {
        System.out.println("================================================================");
        System.out.printf(" PORTFOLIO SUMMARY — %s%n", user.getUsername());
        System.out.println("================================================================");

        if (holdings.isEmpty()) {
            // Friendly empty-state message
            System.out.println(" You don't own any stocks yet.");
            System.out.println(" Use option 2 from the main menu to buy your first stock!");
            System.out.println("----------------------------------------------------------------");
            System.out.printf(" Cash Balance      : %20s%n",
                String.format("$%,.2f", user.getCashBalance()));
            System.out.printf(" Overall Portfolio : %20s%n",
                String.format("$%,.2f", user.getCashBalance()));
            System.out.println("================================================================");
            return;
        }

        // ---- Column headers ----
        System.out.printf(" %-6s  %6s  %14s  %16s%n",
            "Symbol", "Shares", "Price/Share", "Holding Value");
        System.out.println("----------------------------------------------------------------");

        double totalStockValue = 0.0;

        // Sort the holdings alphabetically by symbol for a consistent display
        List<String> sortedSymbols = new ArrayList<>(holdings.keySet());
        Collections.sort(sortedSymbols);

        for (String symbol : sortedSymbols) {
            int    shares       = holdings.get(symbol);
            Stock  stock        = market.getStock(symbol);

            // Guard: if a stock was somehow delisted, show N/A for the price
            if (stock == null) {
                System.out.printf(" %-6s  %6d  %14s  %16s%n",
                    symbol, shares, "N/A", "N/A");
                continue;
            }

            double pricePerShare  = stock.getPrice();
            double holdingValue   = shares * pricePerShare;
            totalStockValue      += holdingValue;

            System.out.printf(" %-6s  %6d  %14s  %16s%n",
                symbol,
                shares,
                String.format("$%,.2f", pricePerShare),
                String.format("$%,.2f", holdingValue));
        }

        // ---- Totals section ----
        double cashBalance       = user.getCashBalance();
        double overallPortfolio  = cashBalance + totalStockValue;

        System.out.println("----------------------------------------------------------------");
        System.out.printf(" %-22s %20s%n",
            "Total Stock Value :",
            String.format("$%,.2f", totalStockValue));
        System.out.printf(" %-22s %20s%n",
            "Cash Balance      :",
            String.format("$%,.2f", cashBalance));
        System.out.println("----------------------------------------------------------------");
        System.out.printf(" %-22s %20s%n",
            "Overall Portfolio :",
            String.format("$%,.2f", overallPortfolio));
        System.out.println("================================================================");
    }
}

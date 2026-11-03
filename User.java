package stocktrading;

/**
 * Represents a registered user of the Stock Trading Platform.
 *
 * Each user has:
 *   - a unique username
 *   - a cash balance they can spend on stocks
 *   - a Portfolio that tracks the stocks they own
 */
public class User {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    // Username never changes after account creation, so it is 'final'.
    private final String username;

    // Cash balance changes every time the user buys or sells stocks.
    private double cashBalance;

    // Every user gets exactly one Portfolio, created automatically at sign-up.
    // It is 'final' because we never swap out the Portfolio object itself —
    // we only modify its contents.
    private final Portfolio portfolio;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new User with the given username and starting cash balance.
     *
     * @param username       the player's display name — must not be null or blank
     * @param initialBalance the amount of cash to start with — must be >= 0
     */
    public User(String username, double initialBalance) {
        // Validate username
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        // Validate starting balance
        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                "Initial cash balance cannot be negative. Received: " + initialBalance);
        }

        this.username     = username.trim();
        this.cashBalance  = initialBalance;
        this.portfolio    = new Portfolio();   // each user starts with an empty portfolio
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the user's display name.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the current cash balance available for trading.
     */
    public double getCashBalance() {
        return cashBalance;
    }

    /**
     * Returns the user's Portfolio (their stock holdings and trade history).
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }

    // -------------------------------------------------------------------------
    // Cash management methods
    // -------------------------------------------------------------------------

    /**
     * Adds money to the user's cash balance (e.g. when a stock is sold,
     * or when the user deposits funds).
     *
     * @param amount the amount to add — must be greater than 0
     */
    public void addCash(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Amount to add must be greater than zero. Received: " + amount);
        }
        cashBalance += amount;
    }

    /**
     * Deducts money from the user's cash balance when buying stocks.
     *
     * This method does NOT throw an exception on failure — instead it
     * returns a boolean so the caller (e.g. StockMarket) can decide
     * what message to show the user.
     *
     * @param amount the amount to deduct — must be greater than 0
     * @return true  if the deduction succeeded (enough funds were available)
     *         false if the user does not have enough cash
     */
    public boolean deductCash(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Amount to deduct must be greater than zero. Received: " + amount);
        }
        // Refuse the deduction if it would push the balance below zero
        if (amount > cashBalance) {
            return false;
        }
        cashBalance -= amount;
        return true;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    /**
     * Returns a readable summary of the user's account.
     *
     * Example output:
     *   Username : Alice
     *   Cash     : $10,000.00
     */
    @Override
    public String toString() {
        return String.format(
            "Username : %s%nCash     : $%,.2f",
            username, cashBalance);
    }
}

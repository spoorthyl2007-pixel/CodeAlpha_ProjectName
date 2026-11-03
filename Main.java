package stocktrading;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Stock Trading Platform console application.
 *
 * Menu options:
 *   1. View Stock Market
 *   2. Buy Stocks
 *   3. Sell Stocks
 *   4. View Portfolio
 *   5. View Account Balance
 *   6. View Transaction History
 *   7. View Watchlist
 *   8. Manage Watchlist  (add / remove)
 *   9. Exit
 */
public class Main {

    private static final double STARTING_BALANCE = 10_000.00;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        printWelcomeBanner();

        String username = askForUsername(scanner);

        User        user      = new User(username, STARTING_BALANCE);
        StockMarket market    = new StockMarket();
        Watchlist   watchlist = new Watchlist();   // each user gets one watchlist

        System.out.printf("%nWelcome, %s! You start with $%,.2f in virtual cash.%n",
                user.getUsername(), user.getCashBalance());

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMenuChoice(scanner);

            switch (choice) {
                case 1 -> viewMarket(market);
                case 2 -> buyStocks(scanner, market, user);
                case 3 -> sellStocks(scanner, market, user);
                case 4 -> viewPortfolio(user, market);
                case 5 -> viewBalance(user);
                case 6 -> viewTransactionHistory(user);
                case 7 -> viewWatchlist(watchlist, market);
                case 8 -> manageWatchlist(scanner, watchlist, market);
                case 9 -> {
                    running = false;
                    printGoodbye(user);
                }
                default -> System.out.println("[!] Invalid choice. Please enter a number from 1 to 9.");
            }
        }

        scanner.close();
    }

    // =========================================================================
    // UI helpers
    // =========================================================================

    private static void printWelcomeBanner() {
        System.out.println("============================================================");
        System.out.println("           STOCK TRADING PLATFORM                          ");
        System.out.println("        CodeAlpha Java Internship — Task 2                 ");
        System.out.println("============================================================");
    }

    private static void printMenu() {
        System.out.println("\n------------------------------------------------------------");
        System.out.println(" MAIN MENU");
        System.out.println("------------------------------------------------------------");
        System.out.println(" 1. View Stock Market");
        System.out.println(" 2. Buy Stocks");
        System.out.println(" 3. Sell Stocks");
        System.out.println(" 4. View Portfolio");
        System.out.println(" 5. View Account Balance");
        System.out.println(" 6. View Transaction History");
        System.out.println(" 7. View Watchlist");
        System.out.println(" 8. Manage Watchlist  (add / remove)");
        System.out.println(" 9. Exit");
        System.out.println("------------------------------------------------------------");
        System.out.print(" Enter your choice: ");
    }

    private static void printGoodbye(User user) {
        System.out.println("\n============================================================");
        System.out.printf(" Goodbye, %s! Thanks for trading with us.%n", user.getUsername());
        System.out.printf(" Final cash balance: $%,.2f%n", user.getCashBalance());
        System.out.println("============================================================");
    }

    // =========================================================================
    // Input helpers
    // =========================================================================

    private static String askForUsername(Scanner scanner) {
        String name = "";
        while (name.isBlank()) {
            System.out.print("\nEnter your username: ");
            name = scanner.nextLine().trim();
            if (name.isBlank()) {
                System.out.println("[!] Username cannot be empty. Please try again.");
            }
        }
        return name;
    }

    private static int readMenuChoice(Scanner scanner) {
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value <= 0) {
                    System.out.println("[!] Quantity must be at least 1. Try again.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a whole number (e.g. 5).");
            }
        }
    }

    private static String readSymbol(Scanner scanner) {
        while (true) {
            System.out.print(" Enter stock symbol (e.g. AAPL): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isBlank()) {
                System.out.println("[!] Symbol cannot be empty. Try again.");
            } else {
                return input;
            }
        }
    }

    // =========================================================================
    // Menu option handlers
    // =========================================================================

    private static void viewMarket(StockMarket market) {
        market.displayMarket();
    }

    private static void buyStocks(Scanner scanner, StockMarket market, User user) {
        System.out.println("\n--- Buy Stocks ---");
        String symbol   = readSymbol(scanner);
        int    quantity = readPositiveInt(scanner, " Enter quantity to buy : ");
        market.buyStock(user, symbol, quantity);
    }

    private static void sellStocks(Scanner scanner, StockMarket market, User user) {
        System.out.println("\n--- Sell Stocks ---");
        String symbol   = readSymbol(scanner);
        int    quantity = readPositiveInt(scanner, " Enter quantity to sell: ");
        market.sellStock(user, symbol, quantity);
    }

    private static void viewPortfolio(User user, StockMarket market) {
        user.getPortfolio().displayPortfolioSummary(user, market);
    }

    private static void viewBalance(User user) {
        System.out.println("\n--- Account Balance ---");
        System.out.println("------------------------------------------------------------");
        System.out.println(user);
        System.out.println("------------------------------------------------------------");
    }

    private static void viewTransactionHistory(User user) {
        System.out.println("\n--- Transaction History ---");
        System.out.println("------------------------------------------------------------");

        List<Transaction> history = (List<Transaction>) user.getPortfolio().getTransactionHistory();

        if (history.isEmpty()) {
            System.out.println(" No transactions yet. Buy or sell a stock to get started!");
        } else {
            int index = 1;
            for (Transaction t : history) {
                System.out.printf(" %2d. %s%n", index++, t);
            }
        }
        System.out.println("------------------------------------------------------------");
    }

    /** Option 7 — Show all watched stocks with live prices. */
    private static void viewWatchlist(Watchlist watchlist, StockMarket market) {
        watchlist.displayWatchlist(market);
    }

    /**
     * Option 8 — Sub-menu: add or remove a stock from the watchlist.
     *
     * Sub-menu:
     *   1. Add stock to watchlist
     *   2. Remove stock from watchlist
     *   3. Back to main menu
     */
    private static void manageWatchlist(Scanner scanner, Watchlist watchlist, StockMarket market) {
        System.out.println("\n--- Manage Watchlist ---");
        System.out.println(" 1. Add stock to watchlist");
        System.out.println(" 2. Remove stock from watchlist");
        System.out.println(" 3. Back to main menu");
        System.out.print(" Enter your choice: ");

        int choice = readMenuChoice(scanner);

        switch (choice) {
            case 1 -> {
                String symbol = readSymbol(scanner);
                watchlist.addStock(symbol, market);
            }
            case 2 -> {
                String symbol = readSymbol(scanner);
                watchlist.removeStock(symbol);
            }
            case 3 -> System.out.println(" Returning to main menu...");
            default -> System.out.println("[!] Invalid choice. Returning to main menu.");
        }
    }
}

import java.io.IOException;
import java.util.List;

import Data.Const;

import Module.SlangDictionary;

import Helper.ConsoleIO;
import Helper.TimeUtil;

public class DictionaryApp {

    /**------------ Fields ------------*/
    private final SlangDictionary dictionary;


    /**------------ Methods ------------*/

    /**
     * Creates a console interface for a loaded dictionary.
     * <p>
     * Stores the supplied dictionary for menu operations. Data loading remains
     * the caller's responsibility before starting the interaction loop.
     * </p>
     *
     * @param tuDien Loaded dictionary used by all console operations
     */
    public DictionaryApp(SlangDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Loads dictionary files and starts the console menu.
     * <p>
     * Measures initial loading, reports the entry count, and starts the menu
     * only when loading succeeds. Prints a source-file hint when startup fails.
     * </p>
     *
     * @param args Command-line arguments; this application does not use them
     */
    public static void main(String[] args) {
        SlangDictionary dict = new SlangDictionary("slang.txt", "slang.dat", "history.dat");

        // Load saved data or build the initial index from the source text.
        try {
            // Measure the time taken to load the dictionary.
            long start = System.nanoTime();
            dict.load();
            double elapsed = TimeUtil.elapsedMillis(start);

            System.out.printf("Loaded %d slangs in %.1f ms.%n", dict.count(), elapsed);

        } catch (IOException e) {
            System.out.println("Failed to load dictionary: " + e.getMessage());
            System.out.println("Please make sure the slang.txt file is in the program's directory.");
            return;
        }

        new DictionaryApp(dict).run();
    }

    /**
     * Displays the menu and dispatches the selected operation.
     * <p>
     * Repeats the ten exercise features until the user chooses zero. Reports
     * file-saving errors and returns to the menu without ending the session.
     * </p>
     */
    private void run() {
        while (true) {
            // Display the same menu choices
            System.out.println();
            System.out.println("Slang dictionary - Menu");
            System.out.println("1. Find by slang");
            System.out.println("2. Find by meaning");
            System.out.println("3. View search history");
            System.out.println("4. Add slang");
            System.out.println("5. Edit slang");
            System.out.println("6. Delete slang");
            System.out.println("7. Restore original list");
            System.out.println("8. Slang of the day and random slang");
            System.out.println("9. Quiz: choose the meaning of a slang");
            System.out.println("10. Quiz: choose the slang by meaning");
            System.out.println("Press any other keys to exit.");

            // Read the user's choice and dispatch the corresponding operation.
            String option = ConsoleIO.input("\nChoose an option: ");

            try {
                switch (option) {
                    case "1":
                        findSlang();
                        break;

                    case "2":
                        findSlangDefinitions();
                        break;

                    case "3":
                        viewHistory();
                        break;

                    case "4":
                        break;

                    case "5":
                        break;

                    case "6":
                        break;

                    case "7":
                        break;

                    case "8":
                        break;

                    case "9":
                        break;

                    case "10":
                        break;

                    default:
                        return; // Exit the loop and end the program
                }

            } catch (IOException e) {
                System.out.println("Error saving data: " + e.getMessage());
            }
        }
    }


    /**------------ Handler Methods ------------*/

    /**
     * Searches for a slang key and displays its meanings.
     * <p>
     * Reads a nonempty key, measures the lookup, and saves its search-history
     * entry. Shows either the stored key spelling and meanings or a no-match
     * message, followed by the lookup duration.
     * </p>
     *
     * @throws IOException If the updated history cannot be saved
     */
    private void findSlang() throws IOException {
        String slang = ConsoleIO.nonemptyInput("Enter the slang word: ");

        // Retrieve the definitions while measuring the time taken for the lookup.
        long start = System.nanoTime();
        List<String> definitions = dictionary.findSlang(slang);
        double elapsed = TimeUtil.elapsedMillis(start);

        // Save the search history
        dictionary.saveHistory();

        // Display the result, showing the original key spelling and meanings if found.
        if (definitions == null) {
            System.out.println("Cannot find \"" + slang + "\".");

        } else {
            System.out.println("Slang word: " + dictionary.getOriginalKey(slang));
            ConsoleIO.outputDefinitions(definitions);
        }

        System.out.printf("Search time: %.3f ms%n", elapsed);
    }

    /**
     * Searches definitions and displays matching slang entries.
     * <p>
     * Measures the definition scan and initially displays at most 100 matching
     * entries. Offers the remaining results only when the user asks to see all
     * matches, preserving the dictionary's key order.
     * </p>
     */
    private void findSlangDefinitions() {
        String keyword = ConsoleIO.nonemptyInput("Enter the keyword in the definition: ");

        // Measure the time taken to find matching slang entries.
        long start = System.nanoTime();
        List<String> definitions = dictionary.findSlangDefinitions(keyword);
        double elapsed = TimeUtil.elapsedMillis(start);

        // Display the number of results and the time taken for the search.
        System.out.printf("Number of results: %d, search time: %.3f ms%n", definitions.size(), elapsed);

        // Get the number of results to display, limited by the constant DISPLAY_LIMIT.
        int count = Math.min(definitions.size(), Const.DISPLAY_LIMIT);
        int remaining = definitions.size() - count;

        // Display the initial set of matching slang entries with their definitions.
        for (int i = 0; i < count; i++) {
            String definition = definitions.get(i);
            System.out.printf("%d. %s%n", i + 1, definition);
        }

        // Display additional matches only when the user requests them.
        if (remaining > 0 && ConsoleIO.question("There are " + remaining + " more results. Display all?")) {
            for (int i = count; i < definitions.size(); i++) {
                String definition = definitions.get(i);
                System.out.printf("%d. %s%n", i + 1, definition);
            }
        }
    }

    /**
     * Displays recent searches and optionally clears them.
     * <p>
     * Lists history from newest to oldest. When the user confirms clearing,
     * removes the records and saves the empty history for later runs.
     * </p>
     *
     * @throws IOException If cleared history cannot be saved
     */
    private void viewHistory() throws IOException {
        // Get the current search history from the dictionary.
        List<String> history = dictionary.getHistory();

        // Show a message when there is no history to display.
        if (history.isEmpty()) {
            System.out.println("No search history.");
            return;
        }

        // List the stored queries in their current recency order.
        System.out.println("Search history, newest first:");

        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, history.get(i));
        }

        // Clear and persist history if the user confirms the action
        if (ConsoleIO.question("Clear search history?")) {
            dictionary.clearHistory();
            dictionary.saveHistory();
            System.out.println("Search history cleared.");
        }
    }
}

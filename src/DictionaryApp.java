import java.io.IOException;
import java.util.Scanner;

import Data.Const;

import Module.SlangStorage;
import Module.SlangDictionary;

public class DictionaryApp {

    /**------------ Fields ------------*/
    private final SlangDictionary dictionary;
    private final Scanner scanner = new Scanner(System.in, "UTF-8");


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
            long now = System.nanoTime();

            System.out.printf("Loaded %d slangs in %.1f ms.%n", dict.count(), (now - start) / 1_000_000.0);

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
            System.out.println("0. Exit");

            // Read the user's choice and dispatch the corresponding operation.
            String option = input("Choose an option: ");

            try {
                switch (option) {
                    case "1":
                        break;

                    case "2":
                        break;

                    case "3":
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

                    case "0":
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (IOException e) {
                System.out.println("Error saving data: " + e.getMessage());
            }
        }
    }


    /**------------ Input Methods ------------*/

    /**
     * Displays a prompt and reads one console line.
     * <p>
     * Uses the application's UTF-8 scanner and returns text without trimming it.
     * Ends the process normally when standard input has no remaining lines.
     * </p>
     *
     * @param notification Prompt displayed before reading
     * @return The next entered line
     */
    private String input(String notification) {
        System.out.print(notification);

        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }

        return scanner.nextLine();
    }
}

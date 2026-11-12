import java.io.IOException;
import java.util.List;

import Data.Const;
import Data.Const.WriteMode;

import Module.SlangDictionary;
import Module.SlangQuestion;

import Helper.ConsoleIO;
import Helper.TimeUtil;
import Helper.SlangEntryParser;

public class DictionaryApp {

    private final SlangDictionary dictionary;
    private final SlangQuestion slangQuestion;


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
        this.slangQuestion = new SlangQuestion(dictionary);
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
            System.out.println("8. Slang of the day");
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
                        addSlang();
                        break;

                    case "5":
                        editSlang();
                        break;

                    case "6":
                        removeSlang();
                        break;

                    case "7":
                        reset();
                        break;

                    case "8":
                        getTodaySlang();
                        break;

                    case "9":
                        slangQuiz();
                        break;

                    case "10":
                        definitionQuiz();
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

        // Retrieve the definitions for the slang
        List<String> definitions = dictionary.findSlang(slang);

        // Save the search history
        dictionary.saveHistory();

        // Display the result, showing the original key spelling and meanings if found.
        if (definitions == null) {
            System.out.println("Cannot find \"" + slang + "\".");

        } else {
            System.out.println("Slang word: " + dictionary.getOriginalKey(slang));
            ConsoleIO.outputDefinitions(definitions);
        }
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

        // Find matching slang entries.
        List<String> definitions = dictionary.findSlangDefinitions(keyword);

        // Display the number of results found
        System.out.printf("Number of results: %d%n", definitions.size());

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

    /**
     * Adds a slang entry or supplies more meanings for an existing key.
     * <p>
     * Reads a nonempty key and at least one definition. Existing keys offer
     * overwrite, append, or cancel choices. Saves a completed change and shows
     * the resulting definition list.
     * </p>
     *
     * @throws IOException If the updated dictionary cannot be saved
     */
    private void addSlang() throws IOException {
        String slang = ConsoleIO.nonemptyInput("Enter slang that you want to add: ");
        List<String> definitions =
            ConsoleIO.inputDefinitions("Enter meanings, separated by \'|\'' if there are more than one: ");

        // Default to overwriting existing meanings, but offer the user a choice when the slang already exists.
        WriteMode mode = Const.WriteMode.OVERWRITE;

        if (dictionary.hasSlang(slang)) {
            // Warn the user that the slang already exists and show its current meanings.
            System.out.println("Slang \'" + dictionary.getOriginalKey(slang) + "\' already exists.");
            ConsoleIO.outputDefinitions(dictionary.getDefinitions(slang));
            System.out.println("1. Overwrite existing meanings");
            System.out.println("2. Append new meanings");
            System.out.println("Press any other key to cancel.");

            String option = ConsoleIO.input("Chon: ");

            // Set the write mode based on the user's choice, or cancel if the input is invalid.
            if (option.equals("1")) {
                mode = Const.WriteMode.OVERWRITE;

            } else if (option.equals("2")) {
                mode = Const.WriteMode.APPEND;

            } else {
                return;
            }
        }

        // Apply the chosen update and update the dictionary file
        List<String> updatedDefinitions = dictionary.addSlang(slang, definitions, mode);
        dictionary.saveDictionary();

        // Display the final meanings for the slang after the operation.
        System.out.println("Saved slang: " + dictionary.getOriginalKey(slang));
        ConsoleIO.outputDefinitions(updatedDefinitions);
    }

    /**
     * Edits an existing slang entry.
     * <p>
     * Displays its current meanings and accepts optional replacement key and
     * definition text. Blank input preserves the corresponding value. Reports
     * key conflicts without saving and persists a successful edit.
     * </p>
     *
     * @throws IOException If the changed dictionary cannot be saved
     */
    private void editSlang() throws IOException {
        String slang = ConsoleIO.nonemptyInput("Enter slang that needs modification: ");

        // Check if the slang exists in the dictionary before proceeding with the edit.
        if (!dictionary.hasSlang(slang)) {
            System.out.println("Cannot find \"" + slang + "\".");
            return;
        }

        // Get the original key spelling and display its current meanings for the user to review.
        String originalName = dictionary.getOriginalKey(slang);
        System.out.println("Current slang: " + originalName);
        ConsoleIO.outputDefinitions(dictionary.getDefinitions(originalName));

        // Prompt the user for new slang and meanings, allowing them to leave fields blank to retain current values.
        String newName = ConsoleIO.input("Enter new slang, or leave blank to keep the current: ").trim();
        String newDefinitionsString =
            ConsoleIO.input("Enter new meanings, separated by | if more than one, or leave blank to keep the current: ");
        List<String> newDefinitions = SlangEntryParser.splitDefinitions(newDefinitionsString);


        // If the user provides an empty list of definitions, treat it as a rename-only operation and retain the current meanings
        if (newDefinitions != null && newDefinitions.isEmpty()) {
            System.out.println("Invalid meanings, keeping the current ones.");
            newDefinitions = null;
        }

        List<String> updatedDefinitions = null;

        // Attempt to edit the slang entry, logging any errors
        // and returning without saving if the operation fails due to invalid input or key conflicts.
        try {
            updatedDefinitions = dictionary.editSlang(originalName, newName, newDefinitions);

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + " Nothing changed.");
            return;
        }

        // Save the updated dictionary
        dictionary.saveDictionary();

        // Display the final slang name and its meanings after the edit operation.
        String finalName = (newName == null || newName.isEmpty()) ? originalName : newName;
        System.out.println("Slang modified from \"" + originalName + "\" to \"" + finalName + "\".");
        ConsoleIO.outputDefinitions(updatedDefinitions);
    }

    /**
     * Deletes a slang entry after confirmation.
     * <p>
     * Checks that the requested key exists, shows its meanings, and retains the
     * original confirmation step before deleting and saving the dictionary.
     * A negative answer leaves the entry unchanged.
     * </p>
     *
     * @throws IOException If the dictionary cannot be saved after deletion
     */
    private void removeSlang() throws IOException {
        String slang = ConsoleIO.nonemptyInput("Enter slang to delete: ");

        // Check if the slang exists in the dictionary before proceeding with the deletion.
        if (!dictionary.hasSlang(slang)) {
            System.out.println("Cannot find \"" + slang + "\".");
            return;
        }

        // Display the original key spelling and its current meanings for the user to review before confirming deletion.
        String originalName = dictionary.getOriginalKey(slang);
        System.out.println("Slang: " + originalName);
        ConsoleIO.outputDefinitions(dictionary.getDefinitions(originalName));

        // Keep deletion and its persisted result behind the existing prompt.
        if (ConsoleIO.question("\nAre you sure you want to delete \"" + originalName + "\"?")) {
            dictionary.removeSlang(originalName);
            dictionary.saveDictionary();
            System.out.println("Deleted.");

        } else {
            System.out.println("Cancelled.");
        }
    }

    /**
     * Restores the dictionary from its original source file.
     * <p>
     * Requests confirmation because existing dictionary edits will be replaced.
     * Reloads and saves the source entries, then reports the restored key count.
     * </p>
     *
     * @throws IOException If reading or saving the source dictionary fails
     */
    private void reset() throws IOException {
        if (ConsoleIO.question("Are you sure you want to restore the original dictionary? All changes will be lost.")) {
            dictionary.reset();
            System.out.println("Original dictionary restored. Number of slang entries: " + dictionary.count());
        }
    }

    /**
     * Displays today's slang and optional additional random entries.
     * <p>
     * Starts with the date-based selection and its meanings.
     * </p>
     */
    private void getTodaySlang() {
        String todaySlang = dictionary.getTodaySlang();
        System.out.println("Today slang is: " + todaySlang);
        ConsoleIO.outputDefinitions(dictionary.getDefinitions(todaySlang));
    }

    /**
     * Runs quizzes that ask for a slang key's meaning.
     * <p>
     * Generates one question at a time, checks the selected definition through
     * the shared answer handler, and offers another round afterward.
     * </p>
     */
    private void slangQuiz() {
        do {
            slangQuestion.buildSlangQuiz();
        } while (ConsoleIO.question("Do you want to play again?"));
    }

    /**
     * Runs quizzes that ask for the slang matching a definition.
     * <p>
     * Generates definition prompts with slang choices, delegates answer checking
     * to the shared handler, and repeats only when the user requests another round.
     * </p>
     */
    private void definitionQuiz() {
        do {
            slangQuestion.buildDefinitionQuiz();
        } while (ConsoleIO.question("Do you want to play again?"));
    }
}

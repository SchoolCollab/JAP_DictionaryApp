package Helper;

import java.util.List;
import java.util.Scanner;

/**
 * Utility methods for console input and output.
 */
public class ConsoleIO {

    /**------------ Fields ------------*/
    private static final Scanner scanner = new Scanner(System.in, "UTF-8");


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
    static public String input(String notification) {
        System.out.print(notification);

        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }

        return scanner.nextLine();
    }

    /**
     * Reads a nonempty trimmed console value.
     * <p>
     * Repeats the supplied prompt until the entered line contains text after
     * surrounding whitespace is removed.
     * </p>
     *
     * @param notification Prompt describing the required input
     * @return A trimmed string containing at least one character
     */
    static public String nonemptyInput(String notification) {
        while (true) {
            String s = input(notification).trim();

            if (!s.isEmpty()) {
                return s;
            }

            System.out.println("Cannot be empty.");
        }
    }

    /**
     * Reads at least one distinct definition from the console.
     * <p>
     * Splits each entered line on pipe characters and retries when no nonempty
     * definitions remain after trimming and duplicate removal.
     * </p>
     *
     * @param notification Prompt describing the definition input format
     * @return A nonempty list of distinct trimmed definitions
     */
    static public List<String> inputDefinitions(String notification) {
        while (true) {
            List<String> definitions = SlangEntryParser.splitDefinitions(input(notification));

            if (!definitions.isEmpty()) {
                return definitions;
            }

            System.out.println("Must enter at least one definition.");
        }
    }


    /**------------ Output Methods ------------*/

    /**
     * Prints a list of definitions in a single line.
     * <p>
     * Joins the definitions with " - " as a separator and prints them after a
     * header indicating whether there is one or multiple definitions.
     * </p>
     *
     * @param definitions List of definitions to print
     */
    static public void outputDefinitions(List<String> definitions) {
        boolean plural = definitions.size() > 1;
        String output = "Definition" + (plural ? "s" : "") + ":";

        // Append each definition with a leading dash for plural cases, or just space for singular.
        for (String definition : definitions) {
            if (plural) {
                output += "\n -";
            }

            output += ' ' + definition;
        }

        System.out.println(output);
    }
}

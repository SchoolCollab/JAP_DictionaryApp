package Helper;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap;

/**
 * Utility class for parsing slang entries.
 *
 * @author Livens
 */
public class SlangEntryParser {

    /**
     * Parses a slang entry with a backtick between the slang term and its definitions.
     * <p>
     * Definitions are separated by pipes; each is trimmed, and duplicate or empty
     * definitions are removed while preserving their original order.
     * <p>
     *
     * @param input the entry to parse, for example {@code term`meaning one|meaning two}
     * @return An entry containing the trimmed slang term and its definitions, or
     *         {@code null} if the input has no backtick
     */
    public static Map.Entry<String, List<String>> parseSlangEntry(String input) {
        // Find the first backtick to separate the slang from its definitions.
        int divider = input.indexOf('`');

        // Ignore lines without a backtick, as they cannot be parsed into a valid entry.
        if (divider < 0) {
            return null;
        }

        // Get the slang key and the definitions
        String slang = input.substring(0, divider).trim();
        String definitions = input.substring(divider + 1).trim();

        // Ignore empty slang key or definitions, as they do not form valid entries
        if (slang.isEmpty() || definitions.isEmpty()) {
            return null;
        }

        return new AbstractMap.SimpleEntry<>(slang, splitDefinitions(definitions));
    }

    /**
     * Splits definition input into distinct nonempty values.
     * <p>
     * Uses pipe separators, trims each piece, and preserves the first occurrence
     * and order of each meaning without changing its letter case.
     * </p>
     *
     * @param definitions Pipe-separated definition input
     * @return A new list of distinct trimmed meanings, possibly empty
     */
    public static List<String> splitDefinitions(String definitions) {
        List<String> ds = new ArrayList<>();

        // Return an empty list if the input is null or contains only whitespace
        if (definitions == null || definitions.trim().isEmpty()) {
            return ds;
        }

        // Keep the first occurrence of each nonempty input definition.
        for (String definition : definitions.split("\\|")) {
            if (!definition.trim().isEmpty() && !ds.contains(definition.trim())) {
                ds.add(definition.trim());
            }
        }

        return ds;
    }
}

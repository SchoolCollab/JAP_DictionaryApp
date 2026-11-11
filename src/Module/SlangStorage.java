package Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class SlangStorage {

    /**
     * Parses the original slang text file.
     * <p>
     * Reads each line and separates each key from its definitions at the first backtick.
     * Repeated keys merge their distinct, pipe-separated definitions.
     * </p>
     *
     * @param file Original slang text file
     * @return A new case-insensitive dictionary containing the parsed entries
     * @throws IOException If the source text cannot be read
     */
    public static TreeMap<String, List<String>> readOriginalFile(File file) throws IOException {
        // Create a case-insensitive map for the slang entries.
        TreeMap<String, List<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        // Read the source as UTF-8 while keeping continuation-line context.
        try
            (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            // Read the first line to check for an optional BOM and the header.
            String currentLine = br.readLine();

            // Make sure that the first line exists and is the expected header
            if (currentLine == null || !currentLine.contains("Slag`Meaning")) {
                return map;
            }

            // Read each line and parse the slang and definitions.
            while ((currentLine = br.readLine()) != null) {
                if ((currentLine = currentLine.trim()).isEmpty()) {
                    continue;
                }

                // Find the first backtick to separate the slang from its definitions.
                int divider = currentLine.indexOf('`');

                // Ignore lines without a backtick, as they cannot be parsed into a valid entry.
                if (divider < 0) {
                    continue;
                }

                // Get the slang key and the definitions
                String slang = currentLine.substring(0, divider).trim();
                String definitions = currentLine.substring(divider + 1).trim();

                // Ignore empty slang key or definitions, as they do not form valid entries
                if (slang.isEmpty() || definitions.isEmpty()) {
                    continue;
                }

                // Get the existing list of definitions for this slang
                List<String> definitionList = map.get(slang);

                // If the slang key is new, create a new list for its definitions
                if (definitionList == null) {
                    definitionList = new ArrayList<>();
                    map.put(slang, definitionList);
                }

                // Split the definitions by pipe and add them to the list
                for (String definition : definitions.split("\\|")) {
                    definition = definition.trim();

                    // Only add non-empty definitions that are not already in the list
                    if (!definition.isEmpty() && !definitionList.contains(definition)) {
                        definitionList.add(definition);
                    }
                }
            }
        }

        return map;
    }
}

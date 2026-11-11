package Module;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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

    /**
     * Writes a serializable object to a file.
     * <p>
     * Opens a buffered object output stream, writes the complete supplied value,
     * and closes the stream. Replaces the previous contents of the target file.
     * </p>
     *
     * @param file Destination object file
     * @param object Serializable dictionary or history value to save
     * @throws IOException If opening, writing, or closing the file fails
     */
    public static void write(File file, Serializable object) throws IOException {
        try
            (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(object);
        }
    }

    /**
     * Reads a previously saved object from a file.
     * <p>
     * Opens a buffered object input stream and returns the deserialized value
     * using the caller's expected type. The generic cast is unchecked, so callers
     * must request a type that matches the file they previously saved.
     * </p>
     *
     * @param <T> Expected type of the stored object
     * @param file Existing serialized object file
     * @return The deserialized object as the requested type
     * @throws IOException If opening or reading the object file fails
     * @throws ClassNotFoundException If a stored object's class cannot be resolved
     */
    @SuppressWarnings("unchecked") // This is to suppress the unchecked cast warning for the generic return type.
    public static <T> T read(File file) throws IOException, ClassNotFoundException {
        try
            (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (T) ois.readObject();
        }
    }
}

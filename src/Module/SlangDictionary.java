package Module;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;

import Data.Const.WriteMode;

public class SlangDictionary {

    private final File originalFile;
    private final File dataFile;
    private final File historyFile;

    private TreeMap<String, List<String>> dictionary;
    private LinkedList<String> history;

    private String[] slangKeysCache;
    private final Random random = new Random();


    /**------------ Methods ------------*/

    /**
     * Configures the files used by this dictionary.
     * <p>
     * Records the source-text, dictionary-data, and history paths. Does not read
     * files yet; call napDuLieu before searching or changing entries.
     * </p>
     *
     * @param originalFile Path to the original slang text file
     * @param dataFile Path to the saved dictionary object file
     * @param historyFile Path to the saved search-history object file
     */
    public SlangDictionary(String originalFile, String dataFile, String historyFile) {
        this.originalFile = new File(originalFile);
        this.dataFile = new File(dataFile);
        this.historyFile = new File(historyFile);
    }

    /**
     * Loads the dictionary and its saved search history.
     * <p>
     * Tries the saved dictionary first and parses the source when no usable
     * dictionary is available. Starts with empty history if its saved file is
     * missing or unreadable, then invalidates the random-selection key cache.
     * </p>
     *
     * @throws IOException If the source cannot be read or its index cannot be saved
     */
    public void load() throws IOException {
        // Reuse the saved dictionary when it can be read successfully
        if (dataFile.exists()) {
            try {
                dictionary = SlangStorage.read(dataFile);

            } catch (ClassNotFoundException | IOException e) {
                System.out.println("Data file is corrupted, loading from original file.");
                dictionary = null;
            }

        } else {
            System.out.println("Data file is missing, loading from original file.");
        }

        // Build and save the dictionary when no usable index is available
        if (dictionary == null) {
            dictionary = SlangStorage.readOriginalFile(originalFile);
            saveDictionary();
        }

        // Restore previous searches without preventing dictionary use on failure
        if (historyFile.exists()) {
            try {
                history = SlangStorage.read(historyFile);

            } catch (ClassNotFoundException | IOException e) {
                history = new LinkedList<>();
            }

        } else {
            history = new LinkedList<>();
        }

        // Reset the cached array of slang keys
        slangKeysCache = null;
    }


    /**------------ Dictionary methods ------------*/

    /**
     * Returns the number of slang entries.
     * <p>
     * Counts distinct keys in the loaded map. Different letter cases refer to
     * the same key because the dictionary uses case-insensitive comparison.
     * </p>
     *
     * @return The current number of distinct slang entries
     */
    public int count() {
        return dictionary.size();
    }

    /**
     * Checks whether a slang key exists.
     * <p>
     * Trims the input and performs a case-insensitive map lookup.
     * </p>
     *
     * @param slang Slang key to check
     * @return True when the key is present, false when absent
     */
    public boolean hasSlang(String slang) {
        return dictionary.containsKey(slang.trim());
    }

    /**
     * Finds the spelling stored for a slang key.
     * <p>
     * Resolves a case-insensitive match through the ordered map and returns its
     * original key text. An absent key returns the caller's trimmed spelling.
     * </p>
     *
     * @param slang Slang key whose stored spelling is requested
     * @return The matching stored key, or the trimmed input when absent
     */
    public String getOriginalKey(String slang) {
        slang = slang.trim();
        Map.Entry<String, List<String>> e = dictionary.ceilingEntry(slang);
        return (e != null && e.getKey().equalsIgnoreCase(slang)) ? e.getKey() : slang;
    }

    /**
     * Returns the meanings stored for a slang key.
     * <p>
     * Trims the input and performs a case-insensitive map lookup. Does not
     * record the query in history.
     * </p>
     *
     * @param slang Slang key to find
     * @return The stored meanings, or null when the key is absent
     */
    public List<String> getDefinitions(String slang) {
        return dictionary.get(slang.trim());
    }

    /**
     * Searches for an exact slang key and records the query.
     * <p>
     * Trims the input, adds it to the front of the history even when no match
     * exists, and performs a case-insensitive map lookup.
     * </p>
     *
     * @param slang Slang key to find
     * @return The stored meanings, or null when the key is absent
     */
    public List<String> findSlang(String slang) {
        slang = slang.trim();
        history.addFirst(slang);
        return dictionary.get(slang);
    }

    /**
     * Finds slang entries containing a definition keyword.
     * <p>
     * Trims and lowercases the keyword, then scans the definitions for substring
     * matches. Adds each matching slang key once, in the map's sorted order.
     * This search does not add an item to slang-search history.
     * </p>
     *
     * @param keyword Keyword or phrase to find within definitions
     * @return A new list of matching slang keys
     */
    public List<String> findSlangDefinitions(String keyword) {
        keyword = keyword.trim().toLowerCase();
        List<String> results = new ArrayList<>();

        // Stop at the first matching meaning so each key appears only once.
        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            for (String meaning : entry.getValue()) {
                if (meaning.toLowerCase().contains(keyword)) {
                    results.add(entry.getKey());
                    break;
                }
            }
        }

        return results;
    }

    /**
     * Adds a slang entry or updates an existing entry's meanings.
     * <p>
     * Copies the supplied meanings for a new entry or overwrite operation.
     * Otherwise appends meanings that are not already in the stored list.
     * </p>
     *
     * @param slang Slang key to add or update
     * @param definitions Meanings supplied for the entry
     * @param mode OVERWRITE to replace meanings, otherwise append distinct meanings
     * @return The stored meanings after the operation
     */
    public List<String> addSlang(String slang, List<String> definitions, WriteMode mode) {
        slang = slang.trim();

        // Retrieve the current meanings for the key, if any.
        List<String> currentDefinitions = dictionary.get(slang);

        // Replace meanings for a new key or an explicit overwrite operation.
        if (currentDefinitions == null || mode == WriteMode.OVERWRITE) {
            dictionary.put(slang, new ArrayList<>(definitions));

        } else {
            // Append only definitions that are not already stored.
            for (String def : definitions) {
                if (!currentDefinitions.contains(def)) {
                    currentDefinitions.add(def);
                }
            }
        }

        // Invalidate the cached array of slang keys
        slangKeysCache = null;

        return dictionary.get(slang);
    }

    /**
     * Edits an existing slang entry's key or meanings.
     * <p>
     * Keeps the old key when the replacement name is null or blank, and keeps
     * its meanings when newDefinitions is null. Rejects a rename to another existing
     * key before removing the original entry.
     * </p>
     *
     * @param slangToEdit Existing slang key to edit
     * @param slangNewName Replacement key, or null or blank to keep the current name
     * @param newDefinitions Replacement meanings, or null to retain the current list
     * @return The stored meanings after the edit, or null when the original key is absent
     * @throws IllegalArgumentException If the new key belongs to another entry
     */
    public List<String> editSlang(String slangToEdit, String slangNewName,
        List<String> newDefinitions) {
        if ((slangNewName == null || slangNewName.trim().isEmpty()) && newDefinitions == null) {
            throw new IllegalArgumentException("No changes provided for the slang key.");
        }

        slangToEdit = slangToEdit.trim();

        // Retrieve the current meanings for the key, if any.
        List<String> currentDefinitions = dictionary.get(slangToEdit);

        if (currentDefinitions == null) {
            return null;
        }

        // Determine the final key and definitions to store
        String finalName = (slangNewName == null) ? slangToEdit : slangNewName.trim();
        List<String> finalDefinitions = (newDefinitions == null) ? currentDefinitions : new ArrayList<>
            (newDefinitions);

        // Check if the new name is already used by another entry
        if (!finalName.equalsIgnoreCase(slangToEdit) && dictionary.containsKey(finalName)) {
            throw new IllegalArgumentException("Slang \"" + finalName + "\" already exists.");
        }

        // Store the renamed or updated entry
        dictionary.remove(slangToEdit);
        dictionary.put(finalName, finalDefinitions);

        // Invalidate the cached array of slang keys
        slangKeysCache = null;

        return dictionary.get(finalName);
    }

    /**
     * Deletes a slang entry.
     * <p>
     * Removes the trimmed, case-insensitive key and invalidates cached random
     * keys. Does not change history or write the dictionary file by itself.
     * </p>
     *
     * @param slang Slang key to delete
     * @return True when an entry was removed, otherwise false
     */
    public boolean removeSlang(String slang) {
        boolean removed = dictionary.remove(slang.trim()) != null;

        if (removed) {
            slangKeysCache = null;
        }

        return removed;
    }

    /**
     * Restores dictionary entries from the original text file.
     * <p>
     * Replaces the in-memory map with freshly parsed source data and saves the
     * restored dictionary. Existing search history is left intact.
     * </p>
     *
     * @throws IOException If reading the source or saving the restored map fails
     */
    public void reset() throws IOException {
        dictionary = SlangStorage.readOriginalFile(originalFile);
        saveDictionary();
    }

    /**
     * Saves the current dictionary index.
     * <p>
     * Serializes the complete case-insensitive map and clears the cached key
     * array so later random selections reflect the current entries.
     * </p>
     *
     * @throws IOException If the dictionary object file cannot be written
     */
    public void saveDictionary() throws IOException {
        SlangStorage.write(dataFile, dictionary);
        slangKeysCache = null;
    }


    /**------------ History methods ------------*/

    /**
     * Returns a read-only view of recent slang searches.
     * <p>
     * Exposes the history in most-recent-first order. Callers cannot change it
     * through this view, but later dictionary searches and clearing are visible.
     * </p>
     *
     * @return An unmodifiable view of the current history
     */
    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * Saves the current search history.
     * <p>
     * Serializes the history independently of dictionary entries,
     * preserving its most-recent-first order for the next application run.
     * </p>
     *
     * @throws IOException If the history object file cannot be written
     */
    public void saveHistory() throws IOException {
        SlangStorage.write(historyFile, history);
    }

    /**
     * Clears the in-memory search history.
     * <p>
     * Removes all recorded queries without changing dictionary entries.
     * Call saveHistory afterward when the cleared history should also be saved.
     * </p>
     */
    public void clearHistory() {
        history.clear();
    }


    /**------------ Random selection methods ------------*/

    /**
     * Supplies the cached keys used for random selection.
     * <p>
     * Builds an array in map order only when the cache has been invalidated.
     * Reuses that array until an editing or saving operation clears it again.
     * </p>
     *
     * @return The current array of dictionary keys
     */
    private String[] getCachedKeys() {
        if (slangKeysCache == null) {
            slangKeysCache = dictionary.keySet().toArray(new String[0]);
        }

        return slangKeysCache;
    }

    /**
     * Selects a random slang key.
     * <p>
     * Chooses an index from the cached key array using this dictionary's random
     * generator. Requires at least one loaded entry and does not record history.
     * </p>
     *
     * @return A randomly selected existing slang key
     */
    public String getRandomSlang() {
        String[] keys = getCachedKeys();
        return keys[random.nextInt(keys.length)];
    }

    /**
     * Selects the slang key associated with the current day.
     * <p>
     * Seeds a local generator with the current date's epoch-day value. Repeated
     * calls on the same day choose the same key while the ordered key set stays
     * unchanged. Requires a nonempty dictionary.
     * </p>
     *
     * @return Today's selected slang key
     */
    public String getTodaySlang() {
        String[] keys = getCachedKeys();
        Random todayRandom = new Random(LocalDate.now().toEpochDay());
        return keys[todayRandom.nextInt(keys.length)];
    }
}

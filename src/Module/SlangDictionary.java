package Module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SlangDictionary {

    private final File originalFile;
    private final File dataFile;
    private final File historyFile;

    private TreeMap<String, List<String>> dictionary;
    private LinkedList<String> history;


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
    public List<String> findByMeaning(String keyword) {
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
    public void deleteHistory() {
        history.clear();
    }
}

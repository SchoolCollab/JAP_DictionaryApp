import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Data.Const.WriteMode;

import Module.SlangDictionary;
import Module.SlangQuestion;

import Helper.TimeUtil;

/**
 * Measures the core operations behind each main-menu option.
 * <p>
 * Uses the original slang text as input and saves benchmark changes in a
 * temporary directory, leaving the application's saved data untouched.
 * Each option is run once, as in the startup timing in DictionaryApp.
 * </p>
 */
public class BenchmarkTest {

    /**
     * Runs the startup and menu-operation benchmarks.
     *
     * @param args Optional path to the original slang text file; defaults to slang.txt
     * @throws IOException If the source cannot be read or temporary data cannot be saved
     */
    public static void main(String[] args) throws IOException {
        Path source = Paths.get(args.length > 0 ? args[0] : "slang.txt").toAbsolutePath();
        Path temporaryDirectory = Files.createTempDirectory("slang-benchmark-");
        Path dataFile = temporaryDirectory.resolve("slang.dat");
        Path historyFile = temporaryDirectory.resolve("history.dat");

        try {
            SlangDictionary dictionary = new SlangDictionary(
                source.toString(), dataFile.toString(), historyFile.toString());

            long start = System.nanoTime();
            dictionary.load();
            System.out.printf("Loaded %d slangs in %.1f ms.%n",
                dictionary.count(), TimeUtil.elapsedMillis(start));

            if (dictionary.count() == 0) {
                throw new IllegalStateException("The source dictionary has no slang entries.");
            }

            SlangQuestion questions = new SlangQuestion(dictionary);
            String sampleSlang = dictionary.getRandomSlang();
            List<String> sampleDefinitions = dictionary.getDefinitions(sampleSlang);
            String keyword = sampleDefinitions.isEmpty() ? "a" : sampleDefinitions.get(0);
            String benchmarkSlang = "__benchmark_" + System.nanoTime();
            String renamedSlang = benchmarkSlang + "_edited";

            benchmark(1, "Find by slang", () -> {
                dictionary.findSlang(sampleSlang);
                dictionary.saveHistory();
            });

            benchmark(2, "Find by meaning", () ->
                dictionary.findSlangDefinitions(keyword));

            benchmark(3, "View and clear history", () -> {
                dictionary.getHistory();
                dictionary.clearHistory();
                dictionary.saveHistory();
            });

            benchmark(4, "Add slang", () -> {
                dictionary.addSlang(benchmarkSlang, List.of("benchmark definition"),
                    WriteMode.OVERWRITE);
                dictionary.saveDictionary();
            });

            benchmark(5, "Edit slang", () -> {
                dictionary.editSlang(benchmarkSlang, renamedSlang,
                    List.of("edited benchmark definition"));
                dictionary.saveDictionary();
            });

            benchmark(6, "Delete slang", () -> {
                dictionary.removeSlang(renamedSlang);
                dictionary.saveDictionary();
            });

            benchmark(7, "Restore original list", dictionary::reset);

            benchmark(8, "Slang of the day", () -> {
                String todaySlang = dictionary.getTodaySlang();
                dictionary.getDefinitions(todaySlang);
            });

            benchmark(9, "Generate slang quiz", questions::createSlangQuestion);
            benchmark(10, "Generate definition quiz", questions::createDefinitionQuestion);

        } finally {
            Files.deleteIfExists(historyFile);
            Files.deleteIfExists(dataFile);
            Files.deleteIfExists(temporaryDirectory);
        }
    }

    /**
     * Measures and prints the time for one menu operation.
     *
     * @param option Menu option number
     * @param name Operation name shown in the result
     * @param task Core operation to measure
     * @throws IOException If the operation cannot save its data
     */
    private static void benchmark(int option, String name, Task task) throws IOException {
        long start = System.nanoTime();
        task.run();
        System.out.printf("Option %d - %s: %.3f ms.%n",
            option, name, TimeUtil.elapsedMillis(start));
    }

    /** Operation that may read or write dictionary files. */
    @FunctionalInterface
    private interface Task {
        void run() throws IOException;
    }
}

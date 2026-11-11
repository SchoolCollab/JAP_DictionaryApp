package Helper;

/**
 * Utility class for benchmarking tasks.
 */
public class BenchmarkHelper {

    /**
     * Executes the task and measures the time taken.
     *
     * @param task The task to benchmark
     * @return The time taken to execute the task in milliseconds
     */
    static public double benchmark(Runnable task) {
        long start = System.nanoTime();
        task.run();
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0; // Return time in milliseconds
    }
}

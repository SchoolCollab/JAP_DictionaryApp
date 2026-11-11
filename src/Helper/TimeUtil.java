package Helper;

/**
 * Utility methods for measuring elapsed time.
 */
public class TimeUtil {

    /**
     * Returns the elapsed time since a {@link System#nanoTime()} reading.
     *
     * @param start the start time from {@code System.nanoTime()}
     * @return the elapsed time in milliseconds
     */
    public static double elapsedMillis(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }
}

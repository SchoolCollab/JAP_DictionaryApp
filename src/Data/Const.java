package Data;

/**
 * A class that contains constants used throughout the application.
 */
public class Const {

    /**
     * The maximum number of results to display at once.
     */
    public static final int DISPLAY_LIMIT = 20;


    /**
     * The maximum number of tries for sampling distractors.
     */
    public static final int MAX_SAMPLING_TRIES = 1000;

    /**
     * Specifies how to write a slang dictionary file.
     * <p>
     * {@link #OVERWRITE} replaces any existing file, while {@link #APPEND} adds to it.
     * </p>
     */
    public static enum WriteMode {
        /** Overwrite any existing file. */
        OVERWRITE,

        /** Append to any existing file. */
        APPEND
    }
}

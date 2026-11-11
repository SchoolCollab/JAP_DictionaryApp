package Data;

/**
 * A class that contains constants used throughout the application.
 */
public class Const {

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

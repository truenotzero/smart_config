package truenotzero.smart_config.api;

import java.io.Reader;
import java.io.Writer;

/**
 * An abstraction around files to allow more than just Disk I/O
 *
 * @author true
 * @since 17 October 2019
 */
public interface ConfigFile {
    /**
     * Check if the file exists
     *
     * @return {@code true} if it exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * Create the file if it doesn't exist already
     *
     * @return {@code true} if created, {@code false} if nothing happened
     */
    boolean create();

    /**
     * Get a {@link Writer} for this file
     *
     * @return The appropriate {@link Writer}
     */
    Writer writer();

    /**
     * Get a {@link Reader} for this file
     *
     * @return The appropriate {@link Reader}
     */
    Reader reader();
}

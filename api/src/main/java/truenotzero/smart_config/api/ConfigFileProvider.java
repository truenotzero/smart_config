package truenotzero.smart_config.api;

import java.io.*;

/**
 * An abstraction to allow special rules for locating files
 *
 * @author truenotzero
 * @since 17 October 2019
 */
public interface ConfigFileProvider {
    /**
     * Get a {@link ConfigFile}
     *
     * @param path The path to the file
     * @return A {@link ConfigFile} instance if found, {@code null} otherwise
     */
    ConfigFile get(String path);
}

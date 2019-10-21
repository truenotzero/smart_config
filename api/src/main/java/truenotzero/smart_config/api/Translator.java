package truenotzero.smart_config.api;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Translates a format (JSON, YAML, Properties, etc) to text using readers and writers
 *
 * @see Reader
 * @see Writer
 */
public interface Translator {
    /**
     * Convert textual data into an object
     *
     * @param reader The reader to fetch object data from
     * @param type The {@link Class} representing the type of the object being read
     * @param <T> The type of the object being decoded
     * @return The decoded object
     * @throws IOException If there was an error while reading
     * @see Reader
     */
    <T> T read(Reader reader, Class<T> type) throws IOException;

    /**
     * Convert an object into textual data
     *
     * @param writer The writer to write object data into
     * @param t The object to encode
     * @param <T> The type of the object to encode
     * @param type The {@link Class} representing the type of the object being written
     * @throws IOException If there was an error while writing
     * @see Writer
     */
    <T> void write(Writer writer, Class<T> type, T t) throws IOException;
}

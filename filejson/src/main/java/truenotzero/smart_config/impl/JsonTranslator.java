package truenotzero.smart_config.impl;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import truenotzero.smart_config.api.Translator;

import java.io.*;

/**
 * JSON capabilities courtesy of the amazing GSON library!
 */
public class JsonTranslator implements Translator {
    private final Gson gson;

    public JsonTranslator(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T read(Reader reader, Class<T> type) throws IOException {
        try {
            return this.gson.fromJson(reader, type);
        } catch (JsonIOException e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T> void write(Writer writer, Class<T> type, T t) throws IOException {
        try {
            String out = gson.toJson(t, type);
            System.out.println("pretty printing json");
            System.out.println(out);
            writer.write(out);
//            this.gson.toJson(t, type, writer);
            writer.flush();
        } catch (JsonIOException e) {
            throw new IOException(e);
        }
    }
}

package truenotzero.smart_config.impl;

import truenotzero.smart_config.api.ConfigFile;

import java.io.*;

/**
 * A wrapper around {@link File}
 * @see FilesystemConfigFileProvider
 */
public class FilesystemConfigFile implements ConfigFile {
    private final File file;

    FilesystemConfigFile(File file) {
        this.file = file;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean create() {
        try {
            return file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Writer writer() {
        try {
            return new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Reader reader() {
        try {
            return new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

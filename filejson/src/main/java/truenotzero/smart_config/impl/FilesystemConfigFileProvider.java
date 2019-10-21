package truenotzero.smart_config.impl;

import truenotzero.smart_config.api.ConfigFile;
import truenotzero.smart_config.api.ConfigFileProvider;

import java.io.*;

/**
 * Provides {@link ConfigFile}'s from the filesystem using the default FileI/O API
 * @see FilesystemConfigFile
 */
public class FilesystemConfigFileProvider implements ConfigFileProvider {
    private final File parentDirectory;
    public FilesystemConfigFileProvider(File parentDirectory) {
        this.parentDirectory = parentDirectory;
        assert parentDirectory.isDirectory();
    }

    @Override
    public ConfigFile get(String path) {
        File f = new File(/*this.parentDirectory,*/ path);
        return new FilesystemConfigFile(f);
    }
}

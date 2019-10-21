package truenotzero.smart_config.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import truenotzero.smart_config.ConfigLoader;
import truenotzero.smart_config.api.ConfigClassLocator;
import truenotzero.smart_config.api.ConfigFileProvider;
import truenotzero.smart_config.api.Translator;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class FilesystemJson {
    /**
     * Create a {@link ConfigLoader} that locates configs from disk
     * <p>Conveniently scans all configs upon construction</p>
     *
     * @param packageRoot The package root to scan for {@code Config} classes
     */
    public static ConfigLoader fromPackageRoot(String packageRoot) throws IOException {
        ConfigClassLocator classLocator = new ReflectionsConfigClassLocator(packageRoot);
        ConfigFileProvider fileProvider = new FilesystemConfigFileProvider(new File("."));
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setLenient() // allows comments
                .setPrettyPrinting()
                .create();
        Translator json = new JsonTranslator(gson);

        ConfigLoader loader = new ConfigLoader(classLocator, fileProvider, json);
        loader.init();
        return loader;
    }
}

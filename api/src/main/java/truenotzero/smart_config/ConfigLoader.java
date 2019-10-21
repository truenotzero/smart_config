package truenotzero.smart_config;

import com.google.gson.Gson;
import truenotzero.smart_config.api.ConfigClassLocator;
import truenotzero.smart_config.api.ConfigFile;
import truenotzero.smart_config.api.ConfigFileProvider;
import truenotzero.smart_config.api.Translator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class used for interaction with config files
 * <p>
 *     Typical use example:
 *     <pre>
 *          package myproject;
 *          public class ConfigLoaderDemo {
 *              public static void main(String[] args) {
 *                  // Specify the root package as the argument
 *                  // This will automatically scan for all classes with the @Config annotation
 *                  // and make sure that all configs are on disk
 *                  ConfigLoader loader = FilesystemJson.fromPackageRoot("myproject");
 *
 *                  // Load all configs from disk into the respective classes
 *                  loader.load();
 *
 *                  // Use your configs as you wish!
 *                  // Not just here, but everywhere in your code
 *
 *                  // Optionally, you can also persist changes to disk
 *                  loader.store();
 *              }
 *          }
 *     </pre>
 * @see Config @Config
 * @see Config.Instance @Instance
 * @see truenotzero.smart_config.impl.FilesystemJson FilesystemJson
 * @see #ConfigLoader(ConfigClassLocator, ConfigFileProvider, Translator)
 */
public class ConfigLoader {
    private static final Class<Config.Instance> INSTANCE_ANNOTATION = Config.Instance.class;
    private final ConfigClassLocator classLocator;
    private final ConfigFileProvider fileLocator;
    private final Translator translator;
    private Set<Class<?>> configs;

    /**
     * Create a {@link ConfigLoader}
     * <p>
     *     This constructor should be used when
     * </p>
     *
     * @param classLocator The {@link ConfigClassLocator} used to locate classes with the {@link Config} annotation
     * @param fileLocator The {@link ConfigFileProvider} used to locate {@link ConfigFile}'s
     * @see truenotzero.smart_config.impl.FilesystemJson
     */
    public ConfigLoader(ConfigClassLocator classLocator, ConfigFileProvider fileLocator, Translator translator) {
        this.classLocator = classLocator;
        this.fileLocator = fileLocator;
        this.translator = translator;
        this.configs =  new HashSet<>();
    }

    /**
     * Convenience method to scan and create nonexistent files
     */
    public void init() throws IOException {
        this.scan();
        Set<Class<?>> nonexistentConfigs = this.verify();
        List<Set<Class<?>>> result = this.createDefault(nonexistentConfigs);
        assert result.get(0).equals(nonexistentConfigs);
        assert result.get(1).isEmpty();
    }

    /**
     * Scan the classpath for config classes
     */
    public void scan() {
       this.configs = this.classLocator.locate();
    }

    /**
     * Get the names of all scanned config classes
     *
     * @return A {@link Set} of {@link String}, where each entry is a config class's name
     */
    public Set<String> scannedConfigNames() {
        return configs.stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Get @Config.path()
     */
    private String getPath(Class<?> clazz) {
        Config annotation = clazz.getAnnotation(ConfigClassLocator.CONFIG_ANNOTATION);
        if (annotation == null) {
            throw new IllegalStateException("Expected @" + ConfigClassLocator.CONFIG_ANNOTATION.getCanonicalName() + " on " + clazz.getCanonicalName());
        }

        return annotation.value();
    }

    /**
     * Get the marked @Instance field
     */
    private <T> Field getInstance(Class<T> clazz) {
        Field instanceField = null;
        for (Field field: clazz.getDeclaredFields()) {
            if (field.getAnnotation(INSTANCE_ANNOTATION) != null) {
                if (instanceField == null) {
                    instanceField = field;
                } else {
                    // Two instance fields not allowed
                    throw new IllegalStateException(clazz.getCanonicalName() + " has multiple fields with @"
                            + ConfigLoader.INSTANCE_ANNOTATION.getCanonicalName());
                }
            }
        }
        if (instanceField == null) {
            throw new IllegalStateException(clazz.getCanonicalName() + " missing field with " + ConfigLoader.INSTANCE_ANNOTATION.getCanonicalName());
        }
        Class<?> instanceFieldClass = instanceField.getType();
        if (!clazz.equals(instanceFieldClass)) {
            // check that the instance is of type T
            String msg = '@' + ConfigLoader.INSTANCE_ANNOTATION.getCanonicalName() + " on field with type other than "
                    + clazz.getCanonicalName();
            throw new IllegalStateException(msg);
        }

        return instanceField;
    }

    /**
     * Verify that all config files have equivalents on disk
     *
     * @return A {@code Set} of {@code Class}'s that do not have equivalents on disk
     */
    private Set<Class<?>> verify() {
        return this.configs.stream()
                .filter(e -> !this.verify(e)) // All that fail verification
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Verify that a config file has an equivalent on disk
     *
     * @param clazz The {@code Class} to verify
     * @return {@code true} if the equivalent exists, {@code false} otherwise
     */
    public boolean verify(Class<?> clazz) {
        String path = this.getPath(clazz);
        return fileLocator.get(path).exists();
    }

    /**
     * Create a default configuration file. If the file already exists, overwrite with the defaults.
     *
     * @return A {@code List} containing two {@code Set}'s of {@code Class}'s: The first with all the configs that
     * were created, the second with all the configs that were overwritten
     */
    private List<Set<Class<?>>> createDefault(Set<Class<?>> classes) throws IOException {
        Set<Class<?>> created = new HashSet<>();
        Set<Class<?>> overwritten = new HashSet<>();
        for (Class<?> clazz : classes) {
            if (this.createDefault(clazz)) {
                created.add(clazz);
            } else {
                overwritten.add(clazz);
            }
        }

        return Arrays.asList(created, overwritten);
    }

    /**
     * Create a default configuration file. If the file already exists, overwrite with the defaults.
     *
     * @param clazz The config's {@link Class} to default
     * @param <T> The type of the {@link Class}
     * @return {@code true} if a file was created, {@code false} if an existing file was overwritten
     */
    public <T> boolean createDefault(Class<T> clazz) throws IOException {
        String path = this.getPath(clazz);
        ConfigFile f = fileLocator.get(path);

        boolean created = f.create();
        try {
            T instance = clazz.newInstance();
            if (instance == null) {
                throw new IllegalStateException(clazz.getCanonicalName() + " has no default constructor");
            }
            this.store(clazz, instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(clazz.getCanonicalName() + " has a non-public default constructor", e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(clazz.getCanonicalName() + " is abstract", e);
        }

        return created;
    }

    /**
     * Load all configs
     */
    public void load() throws IOException {
        for (Class<?> config : this.configs) {
            load(config);
        }
    }

    /**
     * Load a specific config
     * @param clazz The config's class
     */
    public <T> void load(Class<T> clazz) throws IOException {
        String path = this.getPath(clazz);
        Reader reader = this.fileLocator.get(path).reader();
        T it = this.translator.read(reader, clazz);

        Field instanceField = this.getInstance(clazz);
        try {
            instanceField.set(null, it);
        } catch (IllegalAccessException e) {
            String msg = "Inacessible @" + ConfigClassLocator.CONFIG_ANNOTATION.getCanonicalName() + " field in "
                    + clazz.getCanonicalName();
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Store all configs
     */
    public void store() throws IOException {
        for (Class<?> config : this.configs) {
            store(config);
        }
    }

    /**
     * Store a specific config
     *
     * @param clazz The config's {@code Class}
     */
    public <T> void store(Class<T> clazz) throws IOException {
        Field instanceField = this.getInstance(clazz);
        try {
            // This suppression is fine because
            // this.getInstance() will throw if
            //
            @SuppressWarnings("unchecked")
            T instance = (T) instanceField.get(null);
            this.store(clazz, instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     /**
     * Store a specific config
     *
     * @param clazz The config's {@code Class} object
     * @param instance An instance to store from
     * @param <T> The type of the instance and the {@code Class}
     */
    private <T> void store(Class<T> clazz, T instance) throws IOException {
        String path = this.getPath(clazz);
        Writer f = this.fileLocator.get(path).writer();
        this.translator.write(f, clazz, instance);
    }
}

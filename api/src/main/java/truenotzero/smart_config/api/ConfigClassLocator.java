package truenotzero.smart_config.api;

import org.reflections.Reflections;
import truenotzero.smart_config.Config;

import java.util.Set;

/**
 * Used to locate classes with the {@link Config} annotation
 *
 * @author truenotzero
 * @since 17 October 2019
 */
public interface ConfigClassLocator {
    /**
     * Every single valid config class must have this annotation. It is to be used for locating such classes.
     */
    Class<Config> CONFIG_ANNOTATION = Config.class;

    /**
     * Locate all classes with the {@link Config} annotation.<br/>
     * Can be used to specify custom search rules
     *
     * @return The set of config files
     * @see ConfigClassLocator#CONFIG_ANNOTATION
     */
    Set<Class<?>> locate();
}

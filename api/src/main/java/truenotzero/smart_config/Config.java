package truenotzero.smart_config;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Create a smart config file
 *
 * <p>
 * To use this, create a {@code class} implementing the annotation, with a field for each property.
 * Additionally, a {@code static} instance field must be created.
 * <br/>
 * Note that:
 * <ul>
 *     <li>Defaults must be specified by assigning values to the fields</li>
 *     <li>Fields marked {@code transient} or {@code static} are not loaded or stored</li>
 *     <li>The default constructor must be accessible (let it auto-generate unless otherwise needed)</li>
 * </ul>
 * Example code:
 * <pre>
 *     import truenotzero.smart_config.Config;
 *     import truenotzero.smart_config.Config.Instance;
 *
 *     &#064;Config("cfg/my-cfg.json")
 *     public class MyCfg {
 *         public String author = "truenotzero";
 *         public int month = 10;
 *         public int year = 2019;
 *
 *         &#064;Instance
 *         public static MyCfg INSTANCE;
 *     }
 * </pre>
 *
 * @author truenotzero
 * @since 17 October 2019
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Config {
    /**
     * Path on disk to the file
     * @return A string representing the path
     */
    String value();

    /**
     * Used to mark the instance variable for injection. Note that only one field can be tagged with this annotation.
     */
    @Target(FIELD)
    @Retention(RUNTIME)
    @interface Instance {}
}

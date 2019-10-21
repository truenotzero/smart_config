package truenotzero.smart_config.impl;

import org.reflections.Reflections;
import truenotzero.smart_config.api.ConfigClassLocator;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Locate classes using the amazing Reflections library!
 */
public class ReflectionsConfigClassLocator implements ConfigClassLocator {
    private final Reflections reflections;

    public ReflectionsConfigClassLocator(Reflections reflections) {
        this.reflections = reflections;
    }

    public ReflectionsConfigClassLocator(String classpathRoot) {
        this(new Reflections(classpathRoot));
    }

    @Override
    public Set<Class<?>> locate() {
        return this.reflections.getTypesAnnotatedWith(ConfigClassLocator.CONFIG_ANNOTATION);
    }
}

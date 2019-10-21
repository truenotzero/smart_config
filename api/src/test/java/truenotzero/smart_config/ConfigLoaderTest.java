package truenotzero.smart_config;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import truenotzero.smart_config.api.ConfigClassLocator;
import truenotzero.smart_config.api.ConfigFile;
import truenotzero.smart_config.api.ConfigFileProvider;
import truenotzero.smart_config.api.Translator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ConfigLoaderTest {
    @Mock Translator translator;
    @Mock ConfigClassLocator classLocator;
    @Mock ConfigFileProvider fileProvider;
    @Mock ConfigFile configFile;
    @Mock Reader reader;
    @Mock Writer writer;
    @Captor ArgumentCaptor<char[]> writerArg;

    private ConfigLoader cl;
    private Set<Class<?>> configClasses;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        configClasses = new HashSet<>();
        configClasses.add(DummyConfig.class);

        when(classLocator.locate()).thenAnswer(e -> configClasses);

        when(fileProvider.get(DummyConfig.PATH)).thenReturn(configFile);
        when(fileProvider.get(anyString())).thenAnswer(e -> {
            String file = (String) e.getArguments()[0];
            if (DummyConfig.PATH.equals(file)) {
                return configFile;
            } else {
                throw new IllegalArgumentException("unknown file: " + file);
            }
        });

        when(configFile.reader()).thenReturn(reader);
        when(configFile.writer()).thenReturn(writer);

        cl = new ConfigLoader(classLocator, fileProvider, translator);
        cl.scan();
    }

    private String getWriteArg() throws IOException {
        verify(writer, atLeastOnce()).write(writerArg.capture(),anyInt(), anyInt());
        return writerArg.getAllValues().stream()
                .map(String::new)
                .reduce("", String::join);
    }

    @Test
    public void scan() {
        cl.scan();

        Set<String> names = configClasses.stream()
                .map(Class::getName)
                .collect(Collectors.toCollection(HashSet::new));
        assertEquals(names, cl.scannedConfigNames());
    }

    @Test
    public void verifyExistentFile() {
        when(configFile.exists()).thenReturn(true);
        assertTrue(cl.verify(DummyConfig.class));
        verify(configFile).exists();
    }

    @Test
    public void verifyNonexistentFile() {
        when(configFile.exists()).thenReturn(false);
        assertFalse(cl.verify(DummyConfig.class));
        verify(configFile).exists();
    }

    // TODO re-enable test
    //@Test
    public void createDefault() throws IOException {
        when(configFile.create()).thenReturn(true);
        assertTrue(cl.createDefault(DummyConfig.class));
        verify(writer, atLeastOnce()).write(writerArg.capture());

        String expectedJson = "{\"foo\":3840,\"bar\":\"bar\",\"baz\":0.0}";
        // todo assertEquals: expectedJson == writerArg.values()
    }

    // TODO re-enable test
    //@Test
    public void overrideConfig() throws IOException {
        when(configFile.create()).thenReturn(false);
        assertFalse(cl.createDefault(DummyConfig.class));
        verify(writer, atLeastOnce()).write(writerArg.capture());

        String expectedJson = "{\"foo\":3840,\"bar\":\"bar\",\"baz\":0.0}";
        // todo assertEquals: expectedJson == writerArg.values()
    }

    // TODO re-enable test
    //@Test
    public void load() throws IOException {
        String json = ("{\"foo\":69,\"bar\":\"cool\",\"baz\":4.2}");
        StringReader stringReader = new StringReader(json);
        when(reader.read(any(char[].class), anyInt(), anyInt())).thenAnswer(e -> {
            char[] cbuf = e.getArgument(0);
            int off = e.getArgument(1);
            int len = e.getArgument(2);

            return stringReader.read(cbuf, off,len);
        });
        cl.load(DummyConfig.class);
        @SuppressWarnings("unused")
        int ignored = verify(reader, atLeastOnce()).read(any(char[].class), anyInt(), anyInt());
        assertEquals(69, DummyConfig.VALUES.foo);
        assertEquals("cool", DummyConfig.VALUES.bar);
        assertEquals(4.2f, DummyConfig.VALUES.baz, 0.00001f);
    }

    // TODO re-enable test
    //@Test
    public void store() throws IOException {
        DummyConfig.VALUES = new DummyConfig();
        DummyConfig.VALUES.foo = 100;
        DummyConfig.VALUES.bar = "hello, world!";
        DummyConfig.VALUES.baz = 3.95f;
        String expectedJson = "{\"foo\":100,\"bar\":\"hello, world!\",\"baz\":3.95}";
        cl.store(DummyConfig.class);
        assertEquals(expectedJson, this.getWriteArg());
    }

    // no @Config
    private static class NoConfig { }
    @Test(expected = IllegalStateException.class)
    public void verifyConfigAnnotation() throws IOException {
        cl.load(NoConfig.class);
    }

    // no @Instance
    @Config(DummyConfig.PATH)
    private static class NoInstance { }
    @Test(expected = IllegalStateException.class)
    public void verifyAnnotatedInstanceField() throws IOException {
        cl.store(NoInstance.class);
    }

    // multiple @Instance
    @Config(DummyConfig.PATH)
    private static class MultipleInstance {
        @Config.Instance
        static MultipleInstance ONE, TWO;
    }
    @Test(expected = IllegalStateException.class)
    public void verifySingleInstanceField() throws IOException {
        cl.store(MultipleInstance.class);
    }

    // @Instance doesn't match class type
    @Config(DummyConfig.PATH)
    private static class BadInstanceType {
        @Config.Instance
        static Object INSTANCE;
    }
    @Test(expected = IllegalStateException.class)
    public void verifyInstanceType() throws IOException {
        cl.store(BadInstanceType.class);
    }

    // Inaccessible @Instance field
    @Config(DummyConfig.PATH)
    private static class InaccessibleInstance {
        @Config.Instance
        private static InaccessibleInstance INSTANCE;
    }
    @Test(expected = IllegalStateException.class)
    public void verifyAccessibleInstance() throws IOException {
        cl.store(InaccessibleInstance.class);
    }

    // No default constructor
    @Config(DummyConfig.PATH)
    private static class NoDefaultCtor {
        public NoDefaultCtor(NoDefaultCtor it) { }

        @Config.Instance
        private static NoDefaultCtor INSTANCE;
    }
    @Test(expected = IllegalStateException.class)
    public void verifyDefaultCtorExists() throws IOException {
        cl.store(InaccessibleInstance.class);
    }

    // No accessible default constructor
    @Config(DummyConfig.PATH)
    private static class InacessibleDefaultCtor {
        private InacessibleDefaultCtor() { }

        @Config.Instance
        static NoDefaultCtor INSTANCE;
    }
    @Test(expected = IllegalStateException.class)
    public void verifyAccessibleDefaultCtor() throws IOException {
        cl.store(InacessibleDefaultCtor.class);
    }

    // Class is abstract
    @Config(DummyConfig.PATH)
    private static abstract class AbstractClass {
        @Config.Instance
        static NoDefaultCtor INSTANCE;
    }
    @Test(expected = IllegalStateException.class)
    public void verifyConcreteClass() throws IOException {
        cl.store(AbstractClass.class);
    }
}

@SuppressWarnings("unused")
@Config(DummyConfig.PATH)
class DummyConfig {
    static final String PATH = "cfg/dummy_config.json";

    int foo = 0xF00;
    String bar = "bar";
    float baz;
    transient Object ignored;
    static Object alsoIgnored;

    @Config.Instance
    static DummyConfig VALUES;
}

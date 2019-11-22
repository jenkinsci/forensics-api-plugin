package io.jenkins.plugins.forensics.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

import hudson.XmlFile;
import hudson.util.XStream2;

/**
 * Base class that provides the basic setup to read and write entities of a given type using {@link XStream}.
 *
 * @param <T>
 *         type of the entities
 *
 * @author Ullrich Hafner
 */
public abstract class AbstractXmlStream<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractXmlStream.class.getName());

    private final Class<T> type;

    /**
     * Creates a new instance of {@link AbstractXmlStream}.
     *
     * @param type
     *         the type of the elements that are stored and retrieved
     */
    protected AbstractXmlStream(final Class<T> type) {
        this.type = type;
    }

    /**
     * Returns the default value that should be returned if the XML file is broken.
     *
     * @return the default value
     */
    protected abstract T createDefaultValue();

    /**
     * Creates a new {@link XStream2} to serialize an entity of the given type.
     *
     * @return the stream
     */
    protected abstract XStream2 createStream();

    /**
     * Reads the specified {@code file} and creates a new instance of the given type.
     *
     * @param file
     *         path to the file
     *
     * @return the created instance
     */
    public T read(final Path file) {
        return readXml(createFile(file), createDefaultValue());
    }

    /**
     * Writes the specified instance to the given {@code file}.
     *
     * @param file
     *         path to the file
     * @param entity
     *         the entity to write to the file
     */
    public void write(final Path file, final T entity) {
        try {
            createFile(file).write(entity);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to write entity to file " + file, exception);
        }
    }

    private XmlFile createFile(final Path file) {
        return new XmlFile(createStream(), file.toFile());
    }

    private T readXml(final XmlFile dataFile, final T defaultValue) {
        try {
            Object restored = dataFile.read();

            if (type.isInstance(restored)) {
                LOGGER.log(Level.FINE, "Loaded data file " + dataFile);

                return type.cast(restored);
            }
            LOGGER.log(Level.SEVERE, "Failed to load " + dataFile + ", wrong type: " + restored);
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Failed to load " + dataFile, exception);
        }
        return defaultValue; // fallback
    }
}

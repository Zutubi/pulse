package com.zutubi.pulse.core.engine.api;

import java.io.File;
import java.io.OutputStream;

/**
 * The execution context provides access to the information specific to the current
 * processes execution environment.
 */
public interface ExecutionContext
{
    /**
     * The working directory in which the build is executing.
     *
     * @return the file that defines the builds working directory.
     */
    File getWorkingDir();

    /**
     * Get the output stream to which feedback / logging can be written.  Data
     * written to this output stream will end up in the recipe log file
     *
     * @return the output stream to the recipe log, or null if you can not write
     * to it.
     */
    OutputStream getOutputStream();

    /**
     * Get the root level scope associated with this execution context.  For ease of use,
     * numerous convenience methods have been added to this interface that provide access
     * to the contents of the scope through the Execution Context interface.
     *
     * @return the root level scope of this execution context.
     */
    Scope getScope();

    // scope related utility methods.

    String getString(String namespace, String name);

    String getString(String name);

    boolean getBoolean(String namespace, String name, boolean defaultValue);

    boolean getBoolean(String name, boolean defaultValue);

    long getLong(String namespace, String name);

    long getLong(String name);

    File getFile(String namespace, String name);

    File getFile(String name);

    <T> T getValue(String namespace, String name, Class<T> type);

    <T> T getValue(String name, Class<T> type);

    void add(String namespace, Reference reference);

    void add(Reference reference);

    void add(String namespace, ResourceProperty resourceProperty);

    void add(ResourceProperty resourceProperty);

    void addString(String namespace, String name, String value);

    void addString(String name, String value);

    void addValue(String namespace, String name, Object value);

    void addValue(String name, Object value);
}

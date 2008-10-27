package com.zutubi.pulse.core.engine.api;

import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.config.ResourceProperty;

import java.io.File;
import java.io.OutputStream;

/**
 *
 *
 */
public interface ExecutionContext
{
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

    PulseScope getScope();

    void add(String namespace, Reference reference);

    void add(Reference reference);

    void add(String namespace, ResourceProperty resourceProperty);

    void add(ResourceProperty resourceProperty);

    void addString(String namespace, String name, String value);

    void addString(String name, String value);

    void addValue(String namespace, String name, Object value);

    void addValue(String name, Object value);

    File getWorkingDir();

    OutputStream getOutputStream();

    String getVersion();
}

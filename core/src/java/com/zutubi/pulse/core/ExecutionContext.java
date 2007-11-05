package com.zutubi.pulse.core;

import java.io.File;
import java.io.OutputStream;

/**
 * An environment in which commands are executed.  Consists of a scope,
 * working directory and output sink.
 */
public class ExecutionContext
{
    private Scope scope = new Scope();
    private File workingDir = null;
    private OutputStream outputStream = null;
    // TODO: replace this with more generic support for extracting properties
    // from the build
    private String version = null;

    public Reference getReference(String name)
    {
        return scope.getReference(name);
    }

    public String getString(String name)
    {
        return getValue(name, String.class);
    }

    public boolean getBoolean(String name, boolean defaultValue)
    {
        Object value = getValue(name, Object.class);
        if(value == null)
        {
            return defaultValue;
        }
        else if(value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            return Boolean.parseBoolean((String) value);
        }
        else
        {
            return defaultValue;
        }
    }

    public long getLong(String name)
    {
        Object value = getValue(name, Object.class);
        if(value == null)
        {
            return 0;
        }
        else if(value instanceof Long)
        {
            return (Long) value;
        }
        else if (value instanceof String)
        {
            return Long.parseLong((String) value);
        }
        else
        {
            return 0;
        }
    }

    public <T> T getValue(String name, Class<T> type)
    {
        return scope.getReferenceValue(name, type);
    }

    public Scope getScope()
    {
        return scope;
    }

    public void addString(String name, String value)
    {
        scope.add(new Property(name, value));
    }

    public void addValue(String name, Object value)
    {
        scope.add(new GenericReference<Object>(name, value));
    }

    public void pushScope()
    {
        scope = new Scope(scope);
    }

    public void popScope()
    {
        this.scope = this.scope.getParent();
    }

    public File getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(File workingDir)
    {
        this.workingDir = workingDir;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}

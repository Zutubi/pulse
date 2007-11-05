package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;

import java.io.File;
import java.io.OutputStream;

/**
 * An environment in which commands are executed.  Consists of a scope,
 * working directory and output sink.
 */
public class ExecutionContext
{
    // If you add a field, remember to update the copy constructor
    private Scope internalScope;
    private Scope userScope;
    private File workingDir = null;
    private OutputStream outputStream = null;
    // TODO: replace this with more generic support for extracting properties
    // from the build
    private String version = null;

    public ExecutionContext()
    {
        internalScope = new Scope();
        userScope = new Scope();
    }

    public ExecutionContext(ExecutionContext other)
    {
        this.internalScope = other.internalScope.copy();
        this.userScope = other.userScope.copy();
        this.workingDir = other.workingDir;
        this.outputStream = other.outputStream;
        this.version = other.version;
    }

    public Reference getInternalReference(String name)
    {
        return internalScope.getReference(name);
    }

    public Reference getReference(String name)
    {
        Reference ref = userScope.getReference(name);
        if (ref == null)
        {
            ref = internalScope.getReference(name);
        }
        return ref;
    }

    public String getInternalString(String name)
    {
        return getInternalValue(name, String.class);
    }

    public String getString(String name)
    {
        return getValue(name, String.class);
    }

    public boolean getInternalBoolean(String name, boolean defaultValue)
    {
        return asBoolean(getInternalValue(name, Object.class), defaultValue);
    }

    public boolean getBoolean(String name, boolean defaultValue)
    {
        return asBoolean(getValue(name, Object.class), defaultValue);
    }

    private boolean asBoolean(Object value, boolean defaultValue)
    {
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

    public long getInternalLong(String name)
    {
        return asLong(getInternalValue(name, Object.class));
    }

    public long getLong(String name)
    {
        return asLong(getValue(name, Object.class));
    }

    private long asLong(Object value)
    {
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

    public File getInternalFile(String name)
    {
        return asFile(getInternalValue(name, Object.class));
    }

    public File getFile(String name)
    {
        return asFile(getValue(name, Object.class));
    }

    private File asFile(Object value)
    {
        if(value == null)
        {
            return null;
        }
        else if(value instanceof File)
        {
            return (File) value;
        }
        else if (value instanceof String)
        {
            return new File((String) value);
        }
        else
        {
            return null;
        }
    }

    public <T> T getInternalValue(String name, Class<T> type)
    {
        return internalScope.getReferenceValue(name, type);
    }

    public <T> T getValue(String name, Class<T> type)
    {
        T value = userScope.getReferenceValue(name, type);
        if(value == null)
        {
            value = internalScope.getReferenceValue(name, type);
        }
        return value;
    }

    public Scope asScope()
    {
        Scope parent = internalScope.copy();
        Scope leaf = userScope.copy();
        leaf.getRoot().setParent(parent);
        return leaf;
    }

    public void addInternal(Reference reference)
    {
        internalScope.add(reference);
    }

    public void add(Reference reference)
    {
        userScope.add(reference);
    }

    public void addInternal(ResourceProperty resourceProperty)
    {
        internalScope.add(resourceProperty);
    }

    public void add(ResourceProperty resourceProperty)
    {
        userScope.add(resourceProperty);
    }

    public void addInternalString(String name, String value)
    {
        internalScope.add(new Property(name, value));
    }

    public void addString(String name, String value)
    {
        userScope.add(new Property(name, value));
    }

    public void addInternalValue(String name, Object value)
    {
        internalScope.add(new GenericReference<Object>(name, value));
    }

    public void addValue(String name, Object value)
    {
        userScope.add(new GenericReference<Object>(name, value));
    }

    public void pushInternalScope()
    {
        internalScope = new Scope(internalScope);
    }

    public void pushScope()
    {
        userScope = new Scope(userScope);
    }

    public void popInternalScope()
    {
        this.internalScope = this.internalScope.getParent();
    }

    public void popScope()
    {
        this.userScope = this.userScope.getParent();
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

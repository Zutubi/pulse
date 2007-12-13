package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;

import java.io.File;
import java.io.OutputStream;

/**
 * An environment in which commands are executed.  Consists of scopes,
 * working directory and output sink.
 *
 * The scopes are split into internal and external.  This gives us two
 * separate namespaces so that we can ensure user-defined properties do
 * not conflict with Pulse-internal properties.
 */
public class ExecutionContext
{
    // If you add a field, remember to update the copy constructor
    private PulseScope internalScope;
    private PulseScope userScope;
    private PulseScope rootUserScope;
    private File workingDir = null;
    private OutputStream outputStream = null;
    // TODO: replace this with more generic support for extracting properties
    // from the build
    private String version = null;

    public ExecutionContext()
    {
        internalScope = new PulseScope();
        rootUserScope = userScope = new PulseScope(internalScope);
    }

    public ExecutionContext(ExecutionContext other)
    {
        this.internalScope = other.internalScope.copy();
        this.userScope = other.userScope.copyTo(other.rootUserScope.getParent());
        this.rootUserScope = userScope.getRoot();
        this.rootUserScope.setParent(internalScope);

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
        if (value != null)
        {
            if (value instanceof Boolean)
            {
                return (Boolean) value;
            }
            else if (value instanceof String)
            {
                return Boolean.parseBoolean((String) value);
            }
        }

        return defaultValue;
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
        if (value != null)
        {
            if (value instanceof Long)
            {
                return (Long) value;
            }
            else if (value instanceof String)
            {
                try
                {
                    return Long.parseLong((String) value);
                }
                catch (NumberFormatException e)
                {
                    // Fall through
                }
            }
        }

        return 0;
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
        if (value != null)
        {
            if (value instanceof File)
            {
                return (File) value;
            }
            else if (value instanceof String)
            {
                return new File((String) value);
            }
        }

        return null;
    }

    public <T> T getInternalValue(String name, Class<T> type)
    {
        return internalScope.getReferenceValue(name, type);
    }

    public <T> T getValue(String name, Class<T> type)
    {
        return userScope.getReferenceValue(name, type);
    }

    public PulseScope getScope()
    {
        return userScope;
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

    void pushInternalScope()
    {
        internalScope = new PulseScope(internalScope);
        rootUserScope.setParent(internalScope);
    }

    void pushScope()
    {
        userScope = new PulseScope(userScope);
    }

    public void push()
    {
        pushInternalScope();
        pushScope();
    }

    void popInternalScope()
    {
        internalScope = internalScope.getParent();
        rootUserScope.setParent(internalScope);
    }

    void popScope()
    {
        userScope = userScope.getParent();
    }

    public void pop()
    {
        popInternalScope();
        popScope();
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

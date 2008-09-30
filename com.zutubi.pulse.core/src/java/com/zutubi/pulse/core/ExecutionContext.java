package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_USER;
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
    private MultiScopeStack scopeStack;
    /**
     * The base directory for the build.
     */
    private File workingDir = null;
    /**
     * The output stream tied to this execution context.  All build output should be sent
     * via this output stream so that it can be captured and reported to the user.
     */
    private OutputStream outputStream = null;

    // from the build - used by maven to specify the maven version number of the build.  We should
    // pass this value around as a user(?) property rather than directly implemented as a field.
    private String version = null;

    public ExecutionContext()
    {
        scopeStack = new MultiScopeStack(NAMESPACE_INTERNAL, NAMESPACE_USER);
    }

    public ExecutionContext(ExecutionContext other)
    {
        this.scopeStack = new MultiScopeStack(other.scopeStack);
        this.workingDir = other.workingDir;
        this.outputStream = other.outputStream;
        this.version = other.version;
    }

    public String getString(String namespace, String name)
    {
        return getValue(namespace, name, String.class);
    }

    public String getString(String name)
    {
        return getValue(name, String.class);
    }

    public boolean getBoolean(String namespace, String name, boolean defaultValue)
    {
        return asBoolean(getValue(namespace, name, Object.class), defaultValue);
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

    public long getLong(String namespace, String name)
    {
        return asLong(getValue(namespace, name, Object.class));
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

    public File getFile(String namespace, String name)
    {
        return asFile(getValue(namespace, name, Object.class));
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

    public <T> T getValue(String namespace, String name, Class<T> type)
    {
        return scopeStack.getScope(namespace).getReferenceValue(name, type);
    }

    public <T> T getValue(String name, Class<T> type)
    {
        return scopeStack.getScope().getReferenceValue(name, type);
    }

    public PulseScope getScope()
    {
        return scopeStack.getScope();
    }

    public void add(String namespace, Reference reference)
    {
        scopeStack.getScope(namespace).add(reference);
    }

    public void add(Reference reference)
    {
        scopeStack.getScope().add(reference);
    }

    public void add(String namespace, ResourceProperty resourceProperty)
    {
        scopeStack.getScope(namespace).add(resourceProperty);
    }

    public void add(ResourceProperty resourceProperty)
    {
        scopeStack.getScope().add(resourceProperty);
    }

    public void addString(String namespace, String name, String value)
    {
        scopeStack.getScope(namespace).add(new Property(name, value));
    }

    public void addString(String name, String value)
    {
        scopeStack.getScope().add(new Property(name, value));
    }

    public void addValue(String namespace, String name, Object value)
    {
        scopeStack.getScope(namespace).add(new GenericReference<Object>(name, value));
    }

    public void addValue(String name, Object value)
    {
        scopeStack.getScope().add(new GenericReference<Object>(name, value));
    }

    public void push()
    {
        scopeStack.push();
    }

    public void pop()
    {
        scopeStack.pop();
    }

    public void popTo(String label)
    {
        scopeStack.popTo(label);
    }

    public void setLabel(String label)
    {
        scopeStack.setLabel(label);
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

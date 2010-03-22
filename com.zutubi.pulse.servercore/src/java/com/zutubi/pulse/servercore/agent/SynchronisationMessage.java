package com.zutubi.pulse.servercore.agent;

import java.util.Properties;

/**
 * A single message that an agent needs to process to synchronise.  These
 * messages are processed just before an agent becomes idle: i.e. when it
 * comes online, and just after a build completes.
 */
public class SynchronisationMessage
{
    private String typeName;
    private Properties arguments = new Properties();

    /**
     * For hibernate.
     *
     * @see #SynchronisationMessage(String, java.util.Properties)
     */
    public SynchronisationMessage()
    {
    }

    /**
     * Creates a new synchronisation message representing a task of the given
     * type with the given arguments.  These details are used to convert this
     * message into an executable task on the agent side.
     *
     * @param typeName  name of the type of task this message represents
     * @param arguments arguments for the task (name-value string pairs)
     */
    public SynchronisationMessage(String typeName, Properties arguments)
    {
        this.typeName = typeName;
        this.arguments = arguments;
    }

    /**
     * @return the type of task this message represents
     */
    public SynchronisationTask.Type getType()
    {
        return SynchronisationTask.Type.valueOf(typeName);
    }

    public void setType(SynchronisationTask.Type type)
    {
        this.typeName = type.name();
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    /**
     * @return arguments for the task (name-value string pairs)
     */
    public Properties getArguments()
    {
        return arguments;
    }

    public void setArguments(Properties arguments)
    {
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SynchronisationMessage that = (SynchronisationMessage) o;

        if (arguments != null ? !arguments.equals(that.arguments) : that.arguments != null)
        {
            return false;
        }
        if (!typeName.equals(that.typeName))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = typeName != null ? typeName.hashCode() : 0;
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }
}

package com.zutubi.pulse.servercore.agent;

import java.util.Properties;

/**
 * A single message that an agent needs to process to synchronise.  These
 * messages are processed just before an agent becomes idle: i.e. when it
 * comes online, and just after a build completes.
 */
public class SynchronisationMessage
{
    private SynchronisationTask.Type type;
    private Properties arguments = new Properties();

    /**
     * For hibernate.
     *
     * @see #SynchronisationMessage(com.zutubi.pulse.servercore.agent.SynchronisationTask.Type, java.util.Properties)
     */
    public SynchronisationMessage()
    {
    }

    /**
     * Creates a new synchronisation message representing a task of the given
     * type with the given arguments.  These details are used to convert this
     * message into an executable task on the agent side.
     *
     * @param type      type of task this message represents
     * @param arguments arguments for the task (name-value string pairs)
     */
    public SynchronisationMessage(SynchronisationTask.Type type, Properties arguments)
    {
        this.type = type;
        this.arguments = arguments;
    }

    /**
     * @return the type of task this message represents
     */
    public SynchronisationTask.Type getType()
    {
        return type;
    }

    public void setType(SynchronisationTask.Type type)
    {
        this.type = type;
    }

    private String getTypeName()
    {
        return type.name();
    }

    private void setTypeName(String typeName)
    {
        this.type = SynchronisationTask.Type.valueOf(typeName);
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
        if (type != that.type)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }
}

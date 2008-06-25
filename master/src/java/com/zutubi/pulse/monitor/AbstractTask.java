package com.zutubi.pulse.monitor;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public abstract class AbstractTask implements Task
{
    private String name;

    protected AbstractTask(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return getName();
    }

    public List<String> getErrors()
    {
        return new LinkedList<String>();
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public boolean hasFailed()
    {
        return false;
    }

    public void execute() throws TaskException
    {

    }
}

package com.zutubi.pulse.master.monitor;

import java.util.LinkedList;
import java.util.List;

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

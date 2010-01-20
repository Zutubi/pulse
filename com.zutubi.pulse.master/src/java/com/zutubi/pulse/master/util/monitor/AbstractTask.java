package com.zutubi.pulse.master.util.monitor;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractTask implements Task
{
    private String name;
    private List<String> errors;

    protected AbstractTask(String name)
    {
        this.name = name;
        this.errors = new LinkedList<String>();
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
        return errors;
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public boolean hasFailed()
    {
        return errors.size() > 0;
    }

    public void addError(String msg)
    {
        errors.add(msg);
    }

    public void execute() throws TaskException
    {
        
    }
}

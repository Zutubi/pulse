package com.cinnamonbob.core;

public class CommandSnapshot
{
    private String name;
    private TimeStamps stamps;
    private boolean succeeded;
    
    public CommandSnapshot(String name, TimeStamps stamps, boolean succeeded)
    {
        this.name = name;
        this.stamps = stamps;
        this.succeeded = succeeded;
    }

    public String getName()
    {
        return name;
    }
    

    public TimeStamps getStamps()
    {
        return stamps;
    }
    

    public boolean succeeded()
    {
        return succeeded;
    }
    
}

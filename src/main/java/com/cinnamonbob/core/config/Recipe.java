package com.cinnamonbob.core.config;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * 
 *
 */
public class Recipe implements Reference
{
    private String name;

    private List<Command> commands = new LinkedList<Command>();
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public void add(Command c)
    {
        commands.add(c);
    }
    
    public List<Command> getCommands()
    {
        return Collections.unmodifiableList(commands);
    }
}

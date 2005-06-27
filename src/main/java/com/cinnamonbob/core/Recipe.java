package com.cinnamonbob.core;

import com.cinnamonbob.core.config.Reference;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Recipe is a sequence of commands that may be used to build a project.
 */
public class Recipe implements Iterable<CommandCommon>, Reference
{
    /**
     * The sequence of commands required to build the recipe.
     */
    private List<CommandCommon> commands = new LinkedList<CommandCommon>();
    
    private String name = "";
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public Recipe()
    {
    }

    public void addCommand(CommandCommon c)
    {
        commands.add(c);
    }
    
    /**
     * Returns an iterator of the commands in this recipe.
     */
    public Iterator<CommandCommon> iterator()
    {
        return commands.iterator();
    }
}

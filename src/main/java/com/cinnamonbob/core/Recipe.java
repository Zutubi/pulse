package com.cinnamonbob.core;

import nu.xom.Element;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Recipe is a sequence of commands that may be used to build a project.
 */
public class Recipe implements Iterable<CommandCommon>
{
    /**
     * The sequence of commands required to build the recipe.
     */
    private List<CommandCommon> commands;
    

    public Recipe(ConfigContext context, Element element, Project project) throws ConfigException
    {
        commands = new LinkedList<CommandCommon>();
        loadCommands(context, element, project);
    }


    private void loadCommands(ConfigContext context, Element element, Project project) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList("command"));
        
        for(Element current: elements)
        {
            CommandCommon command = new CommandCommon(context, current, project);
            commands.add(command);
        }
    }


    /**
     * Returns an iterator of the commands in this recipe.
     */
    public Iterator<CommandCommon> iterator()
    {
        return commands.iterator();
    }
}

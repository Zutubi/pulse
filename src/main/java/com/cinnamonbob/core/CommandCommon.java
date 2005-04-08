package com.cinnamonbob.core;

import nu.xom.Element;

import java.io.File;
import java.util.List;

/**
 * @author jsankey
 */
public class CommandCommon
{
    private static final String CONFIG_ATTR_NAME = "name";

    private String name;
    private Command command;
    
    
    public CommandCommon(String filename, Element element, CommandFactory commandFactory) throws ConfigException
    {
        name = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_NAME);
        
        List<Element> childElements = XMLConfigUtils.getElements(filename, element);
        
        if(childElements.size() == 0)
        {
            throw new ConfigException(filename, "Command '" + name + "' contains no child elements.");
        }
        
        // The first child is the specific command element
        command = commandFactory.createCommand(childElements.get(0).getLocalName(), filename, childElements.get(0), this);
    }
    
    
    /**
     * Returns the name of this command.
     * 
     * @return the name of this command
     */
    public String getName()
    {
        return name;
    }

    
    public CommandResultCommon execute(File outputDir) throws InternalBuildFailureException
    {
        long          startTime     = System.currentTimeMillis();
        CommandResult commandResult = command.execute(outputDir);
        
        return new CommandResultCommon(name, commandResult, new TimeStamps(startTime, System.currentTimeMillis()));
    }
}

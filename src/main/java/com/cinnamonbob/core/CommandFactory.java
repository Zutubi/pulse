package com.cinnamonbob.core;

import nu.xom.Element;


/**
 * @author jsankey
 */
public class CommandFactory extends GenericFactory<Command>
{
    public CommandFactory()
    {
        super(Command.class, CommandCommon.class);
    }
    
    public Command createCommand(String name, String filename, Element element, CommandCommon common) throws ConfigException
    {
        return (Command)super.create(name, filename, element, common);
    }
}

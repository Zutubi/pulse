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
    
    public Command createCommand(String name, ConfigContext context, Element element, CommandCommon common) throws ConfigException
    {
        return (Command)super.create(name, context, element, common);
    }
}

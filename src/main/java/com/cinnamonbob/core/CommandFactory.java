package com.cinnamonbob.core;

import nu.xom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jsankey
 */
public class CommandFactory
{
    private Map<String, Class> nameToTypeMap;
    
    
    public CommandFactory()
    {
        nameToTypeMap = new TreeMap<String, Class>();
    }
    
    
    public boolean registerType(String name, Class clazz)
    {
        if(!Command.class.isAssignableFrom(clazz))
        {
            return false;
        }

        try
        {
            Constructor constructor = clazz.getConstructor(String.class, Element.class, CommandCommon.class);
        }
        catch(NoSuchMethodException e)
        {
            return false;
        }
        
        int modifiers = clazz.getModifiers();
        if(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return false;
        }
        
        nameToTypeMap.put(name, clazz);
        return true;
    }
    
    
    public Command createCommand(String name, String filename, Element element, CommandCommon common) throws ConfigException
    {
        Command command = null;
        
        if(nameToTypeMap.containsKey(name))
        {
            Class clazz = nameToTypeMap.get(name);
            
            try
            {
                Constructor constructor = clazz.getConstructor(String.class, Element.class, CommandCommon.class);
                command = (Command)constructor.newInstance(filename, element, common);
            }
            catch(NoSuchMethodException e)
            {
                throw new ConfigException(filename, "Could not instantiate command from class '" + name + "': Constructor not found.");
            }
            catch(IllegalArgumentException e)
            {
                throw new ConfigException(filename, "Could not instantiate command from class '" + name + "': Illegal argument.");
            }
            catch(InstantiationException e)
            {
                throw new ConfigException(filename, "Could not instantiate command from class '" + name + "': Class not instantiable.");
            }
            catch(IllegalAccessException e)
            {
                throw new ConfigException(filename, "Could not instantiate command from class '" + name + "': Cannot access constructor.");
            }
            catch(InvocationTargetException e)
            {
                Throwable cause = e.getCause();
                if(cause instanceof ConfigException)
                {
                    throw (ConfigException)cause;
                }
                else
                {
                    throw new ConfigException(filename, "Could not instantiate command from class '" + name + "': " + e.getMessage());
                }
            }
        }
        else
        {
            throw new ConfigException(filename, "Unknown command type '" + name + "'");
        }
        
        return command;
    }
}

package com.cinnamonbob.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import nu.xom.Element;

public class GenericFactory<T>
{
    private static final Class[] COMMON_TYPES = { String.class, Element.class };
    
    private Class<T> returnedType;
    private Class[] constructorTypes;
    private Map<String, Class> nameToTypeMap;
    
    
    protected GenericFactory(Class<T> returnedType, Class ...constructorTypes)
    {
        this.returnedType = returnedType;
        this.constructorTypes = new Class[COMMON_TYPES.length + constructorTypes.length];
        System.arraycopy(COMMON_TYPES, 0, this.constructorTypes, 0, COMMON_TYPES.length);
        System.arraycopy(constructorTypes, 0, this.constructorTypes, COMMON_TYPES.length, constructorTypes.length);
        nameToTypeMap = new TreeMap<String, Class>();
    }
    
    
    public boolean registerType(String name, Class clazz)
    {
        // TODO more informative errors needed for user extension
        if(!returnedType.isAssignableFrom(clazz))
        {
            return false;
        }

        try
        {
            Constructor constructor = clazz.getConstructor(constructorTypes);
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
    
    
    protected Object create(String name, String filename, Element element, Object ...args) throws ConfigException
    {
        Object result = null;
        
        if(nameToTypeMap.containsKey(name))
        {
            Class clazz = nameToTypeMap.get(name);
            
            try
            {
                Constructor constructor = clazz.getConstructor(constructorTypes);
                Object fullArgs[] = new Object[COMMON_TYPES.length + args.length];
                
                fullArgs[0] = filename;
                fullArgs[1] = element;
                System.arraycopy(args, 0, fullArgs, COMMON_TYPES.length, args.length);
                
                result = constructor.newInstance(fullArgs);
            }
            catch(NoSuchMethodException e)
            {
                throw new ConfigException(filename, "Could not instantiate " + returnedType.getSimpleName() + " from class '" + name + "': Constructor not found.");
            }
            catch(IllegalArgumentException e)
            {
                throw new ConfigException(filename, "Could not instantiate " + returnedType.getSimpleName() + " from class '" + name + "': Illegal argument.");
            }
            catch(InstantiationException e)
            {
                throw new ConfigException(filename, "Could not instantiate " + returnedType.getSimpleName() + " from class '" + name + "': Class not instantiable.");
            }
            catch(IllegalAccessException e)
            {
                throw new ConfigException(filename, "Could not instantiate " + returnedType.getSimpleName() + " from class '" + name + "': Cannot access constructor.");
            }
            catch(InvocationTargetException e)
            {
                Throwable cause = e.getTargetException();
                if(cause instanceof ConfigException)
                {
                    throw (ConfigException)cause;
                }
                else
                {
                    throw new ConfigException(filename, "Could not instantiate " + returnedType.getSimpleName() + " from class '" + name + "': " + cause.getMessage());
                }
            }
        }
        else
        {
            throw new ConfigException(filename, "Unknown " + returnedType.getSimpleName() + " type '" + name + "'");
        }
        
        return result;
    }
}

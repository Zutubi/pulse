package com.zutubi.prototype.config;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.prototype.type.TypeConversionSupport;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.TypeException;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;

/**
 *
 *
 */
public class ConfigurationCrudSupport
{
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public ConfigurationCrudSupport()
    {
        ComponentContext.autowire(this);
    }

    public void save(String symbolicName, String path, Map parameters) throws TypeException
    {
        try
        {
            Object instance = configurationPersistenceManager.getInstance(path);
            if (instance == null)
            {
                Type type = typeRegistry.getType(symbolicName);
                instance = type.getClazz().newInstance();
            }

            apply(ActionContext.getContext().getParameters(), instance);
            
            // validate.

            configurationPersistenceManager.setInstance(path, instance);
        }
        catch (Exception e)
        {
            throw new TypeException(e);
        }
    }

    public void apply(Map parameters, Object instance) throws TypeException
    {
        TypeConversionSupport conversionSupport = new TypeConversionSupport();
        conversionSupport.setTypeRegistry(typeRegistry);
        conversionSupport.applyMapTo(parameters, instance);
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

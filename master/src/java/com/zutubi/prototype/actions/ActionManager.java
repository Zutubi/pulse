package com.zutubi.prototype.actions;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides support for executing actions on configuration instances.  For
 * example, triggering a project build is an action on a ProjectConfiguration
 * instance.  Actions are presented as links in the web UI, and a form may
 * optionally be displayed to capture an action argument.
 */
public class ActionManager
{
    private static final Logger LOG = Logger.getLogger(ActionManager.class);

    private Map<CompositeType, ConfigurationActions> actionsByType = new HashMap<CompositeType, ConfigurationActions>();
    private ObjectFactory objectFactory;
    private TypeRegistry typeRegistry;

    public List<String> getActions(Object configurationInstance)
    {
        CompositeType type = getType(configurationInstance);
        ConfigurationActions actions = getConfigurationActions(type);
        try
        {
            return actions.getActions(configurationInstance);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return Collections.EMPTY_LIST;
        }
    }

    public void execute(String actionName, Object configurationInstance)
    {
        CompositeType type = getType(configurationInstance);
        ConfigurationActions actions = getConfigurationActions(type);
        try
        {
            actions.execute(actionName, configurationInstance, null);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    private CompositeType getType(Object configurationInstance)
    {
        CompositeType type = typeRegistry.getType(configurationInstance.getClass());
        if(type == null)
        {
            throw new IllegalArgumentException("Invalid instance: not of configuration type");
        }
        return type;
    }

    public synchronized ConfigurationActions getConfigurationActions(CompositeType type)
    {
        ConfigurationActions actions = actionsByType.get(type);
        if(actions == null)
        {
            Class configurationClass = type.getClazz();
            actions = new ConfigurationActions(configurationClass, ConventionSupport.getActions(configurationClass), objectFactory);
            actionsByType.put(type, actions);
        }

        return actions;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

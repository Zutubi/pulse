package com.zutubi.prototype.actions;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;

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
    private ConfigurationSecurityManager configurationSecurityManager;

    public List<String> getActions(Configuration configurationInstance, boolean includeDefault)
    {
        List<String> result = new LinkedList<String>();

        if (configurationInstance != null)
        {
            String path = configurationInstance.getConfigurationPath();
            if (includeDefault)
            {
                if(configurationSecurityManager.hasPermission(path, AccessManager.ACTION_VIEW))
                {
                    result.add(AccessManager.ACTION_VIEW);
                }

                if(!configurationInstance.isPermanent() && configurationSecurityManager.hasPermission(path, AccessManager.ACTION_DELETE))
                {
                    result.add(AccessManager.ACTION_DELETE);
                }
            }

            if (configurationInstance.isConcrete())
            {
                CompositeType type = getType(configurationInstance);
                ConfigurationActions configurationActions = getConfigurationActions(type);
                try
                {
                    List<ConfigurationAction> actions = configurationActions.getActions(configurationInstance);
                    for (ConfigurationAction action : actions)
                    {
                        if (configurationSecurityManager.hasPermission(path, action.getPermissionName()))
                        {
                            result.add(action.getName());
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.severe(e);
                }
            }
        }

        return result;
    }

    public void execute(String actionName, Configuration configurationInstance, Configuration argumentInstance)
    {
        CompositeType type = getType(configurationInstance);
        ConfigurationActions actions = getConfigurationActions(type);
        ConfigurationAction action = actions.getAction(actionName);

        if (action != null)
        {
            configurationSecurityManager.ensurePermission(configurationInstance.getConfigurationPath(), action.getPermissionName());

            try
            {
                actions.execute(actionName, configurationInstance, argumentInstance);
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }
        }
        else
        {
            LOG.warning("Request for unrecognised action '" + actionName + "' on path '" + configurationInstance.getConfigurationPath() + "'");
        }
    }

    private CompositeType getType(Object configurationInstance)
    {
        CompositeType type = typeRegistry.getType(configurationInstance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Invalid instance: not of configuration type");
        }
        return type;
    }

    public synchronized ConfigurationActions getConfigurationActions(CompositeType type)
    {
        ConfigurationActions actions = actionsByType.get(type);
        if (actions == null)
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

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }
}

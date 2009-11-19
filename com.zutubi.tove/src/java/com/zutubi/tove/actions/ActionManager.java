package com.zutubi.tove.actions;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.UnaryFunctionE;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
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

    public static final String I18N_KEY_SUFFIX_FEEDACK = ".feedback";

    private Map<CompositeType, ConfigurationActions> actionsByType = new HashMap<CompositeType, ConfigurationActions>();
    private ObjectFactory objectFactory;
    private TypeRegistry typeRegistry;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationRefactoringManager configurationRefactoringManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public List<String> getActions(Configuration configurationInstance, boolean includeDefault)
    {
        List<String> result = new LinkedList<String>();

        if (configurationInstance != null)
        {
            String path = configurationInstance.getConfigurationPath();
            if (includeDefault)
            {
                if (configurationSecurityManager.hasPermission(path, AccessManager.ACTION_VIEW))
                {
                    result.add(AccessManager.ACTION_VIEW);
                }

                if (configurationRefactoringManager.canClone(path) && configurationSecurityManager.hasPermission(PathUtils.getParentPath(path), AccessManager.ACTION_CREATE))
                {
                    result.add(ConfigurationRefactoringManager.ACTION_CLONE);
                }

                if (configurationRefactoringManager.canPullUp(path))
                {
                    result.add(ConfigurationRefactoringManager.ACTION_PULL_UP);
                }

                if (configurationTemplateManager.canDelete(path) && configurationSecurityManager.hasPermission(path, AccessManager.ACTION_DELETE))
                {
                    result.add(AccessManager.ACTION_DELETE);
                }
            }

            CompositeType type = getType(configurationInstance);
            ConfigurationActions configurationActions = getConfigurationActions(type);

            if (configurationActions.actionsEnabled(configurationInstance, configurationTemplateManager.isDeeplyValid(path)))
            {
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

    public void ensurePermission(String path, String actionName)
    {
        CompositeType type = configurationTemplateManager.getType(path, CompositeType.class);
        ConfigurationActions actions = getConfigurationActions(type);
        ConfigurationAction action = actions.getAction(actionName);

        if (action != null)
        {
            configurationSecurityManager.ensurePermission(path, action.getPermissionName());
        }
        else
        {
            LOG.warning("Permission check for unrecognised action '" + actionName + "' on path '" + path + "'");
        }
    }

    public String getCustomiseName(final String actionName, final Configuration configurationInstance)
    {
        return processAction(actionName, configurationInstance, new UnaryFunctionE<ConfigurationActions, String, Exception>()
        {
            public String process(ConfigurationActions actions) throws Exception
            {
                return actions.customise(actionName, configurationInstance);
            }
        });
    }

    public Configuration prepare(final String actionName, final Configuration configurationInstance)
    {
        return processAction(actionName, configurationInstance, new UnaryFunctionE<ConfigurationActions, Configuration, Exception>()
        {
            public Configuration process(ConfigurationActions actions) throws Exception
            {
                return actions.prepare(actionName, configurationInstance);
            }
        });
    }

    public ActionResult execute(final String actionName, final Configuration configurationInstance, final Configuration argumentInstance)
    {
        return processAction(actionName, configurationInstance, new UnaryFunctionE<ConfigurationActions, ActionResult, Exception>()
        {
            public ActionResult process(ConfigurationActions actions) throws Exception
            {
                return actions.execute(actionName, configurationInstance, argumentInstance);
            }
        });
    }

    private <T> T processAction(String actionName, Configuration configurationInstance, UnaryFunctionE<ConfigurationActions, T, Exception> f)
    {
        CompositeType type = getType(configurationInstance);
        ConfigurationActions actions = getConfigurationActions(type);
        ConfigurationAction action = actions.getAction(actionName);

        if (action != null)
        {
            configurationSecurityManager.ensurePermission(configurationInstance.getConfigurationPath(), action.getPermissionName());

            try
            {
                return f.process(actions);
            }
            catch (Exception e)
            {
                LOG.severe(e);
                throw new RuntimeException(e);
            }
        }
        else
        {
            throw new IllegalArgumentException("Request for unrecognised action '" + actionName + "' on path '" + configurationInstance.getConfigurationPath() + "'");
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
            Class<? extends Configuration> configurationClass = type.getClazz();
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

    public void setConfigurationRefactoringManager(ConfigurationRefactoringManager configurationRefactoringManager)
    {
        this.configurationRefactoringManager = configurationRefactoringManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}

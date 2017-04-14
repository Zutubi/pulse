/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.ui.actions;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.api.ActionVariant;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.UnaryFunctionE;
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

    public static final String I18N_KEY_SUFFIX_FEEDACK = ".feedback";

    // Simple actions require no argument
    private static final Set<String> SIMPLE_COMMON_ACTIONS = new HashSet<>();
    static
    {
        SIMPLE_COMMON_ACTIONS.add(AccessManager.ACTION_VIEW);
        SIMPLE_COMMON_ACTIONS.add(AccessManager.ACTION_WRITE);
        SIMPLE_COMMON_ACTIONS.add(AccessManager.ACTION_DELETE);
    }
    // Complex actions need an input argument.
    private static final Set<String> COMPLEX_COMMON_ACTIONS = new HashSet<>();
    static
    {
        COMPLEX_COMMON_ACTIONS.add(ConfigurationRefactoringManager.ACTION_CLONE);
        COMPLEX_COMMON_ACTIONS.add(ConfigurationRefactoringManager.ACTION_PULL_UP);
        COMPLEX_COMMON_ACTIONS.add(ConfigurationRefactoringManager.ACTION_PUSH_DOWN);
    }

    private Map<CompositeType, ConfigurationActions> actionsByType = new HashMap<>();
    private ObjectFactory objectFactory;
    private TypeRegistry typeRegistry;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationRefactoringManager configurationRefactoringManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public List<String> getActions(Configuration configurationInstance, boolean includeDefault, boolean includeNonSimple)
    {
        List<String> result = new ArrayList<>();

        if (configurationInstance != null)
        {
            String path = configurationInstance.getConfigurationPath();
            if (includeDefault)
            {
                if (configurationSecurityManager.hasPermission(path, AccessManager.ACTION_VIEW))
                {
                    result.add(AccessManager.ACTION_VIEW);
                }

                if (configurationSecurityManager.hasPermission(path, AccessManager.ACTION_WRITE))
                {
                    result.add(AccessManager.ACTION_WRITE);
                }

                if (includeNonSimple)
                {
                    if (configurationRefactoringManager.canClone(path) && configurationSecurityManager.hasPermission(PathUtils.getParentPath(path), AccessManager.ACTION_CREATE))
                    {
                        result.add(ConfigurationRefactoringManager.ACTION_CLONE);
                    }

                    if (configurationRefactoringManager.canPullUp(path))
                    {
                        // The pull up action checks permission to create in target ancestors, as it is the only way to
                        // know if the action is valid.  (This contrasts with other cRM.can* methods which don't do
                        // permission checks, it's a bit messy really.)
                        result.add(ConfigurationRefactoringManager.ACTION_PULL_UP);
                    }

                    if (configurationRefactoringManager.canPushDown(path) && configurationSecurityManager.hasPermission(path, AccessManager.ACTION_DELETE))
                    {
                        result.add(ConfigurationRefactoringManager.ACTION_PUSH_DOWN);
                    }
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
                        if ((includeNonSimple || isSimple(action)) && configurationSecurityManager.hasPermission(path, action.getPermissionName()))
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

    private boolean isSimple(ConfigurationAction action)
    {
        return !action.hasArgument() && !action.hasVariants();
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

    public List<ActionVariant> getVariants(final String actionName, final Configuration configurationInstance)
    {
        if (SIMPLE_COMMON_ACTIONS.contains(actionName) || COMPLEX_COMMON_ACTIONS.contains(actionName))
        {
            return null;
        }

        return processAction(actionName, configurationInstance, new UnaryFunctionE<ConfigurationActions, List<ActionVariant>, Exception>()
        {
            public List<ActionVariant> process(ConfigurationActions actions) throws Exception
            {
                return actions.getVariants(actionName, configurationInstance);
            }
        });
    }

    public boolean hasArgument(final String actionName, final CompositeType type)
    {
        if (COMPLEX_COMMON_ACTIONS.contains(actionName))
        {
            return true;
        }

        ConfigurationActions configurationActions = getConfigurationActions(type);
        ConfigurationAction configurationAction = configurationActions.getAction(actionName);
        return configurationAction != null && configurationAction.hasArgument();
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

    /**
     * Executes the given action on all concrete descendants of the given path
     * that have the action currently avaialable.  The action must be simple -
     * i.e. it cannot require an argument or use a custom UI.
     *
     * @param actionName name of the action to execute
     * @param path       path to process the concrete descendants of
     * @return a mapping from path to result for all concrete descendants on
     *         which the action was performed
     */
    public Map<String, ActionResult> executeOnDescendants(String actionName, String path)
    {
        Map<String, ActionResult> results = new HashMap<String, ActionResult>();
        List<String> concreteDescendantPaths = configurationTemplateManager.getDescendantPaths(path, true, true, false);
        for (String descendantPath: concreteDescendantPaths)
        {
            Configuration descendant = configurationTemplateManager.getInstance(descendantPath);
            if (descendant != null && getActions(descendant, false, false).contains(actionName))
            {
                results.put(descendantPath, execute(actionName, descendant, null));
            }
        }

        return results;
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
            actions = new ConfigurationActions(configurationClass, ConventionSupport.loadClass(configurationClass, "Actions", Object.class), objectFactory);
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

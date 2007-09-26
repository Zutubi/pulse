package com.zutubi.prototype.actions;

import com.zutubi.config.annotations.Permission;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.ReflectionUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Describes a bunch of available actions for a configuration type.
 */
public class ConfigurationActions
{
    private static final Logger LOG = Logger.getLogger(ConfigurationActions.class);

    private Class configurationClass;
    private Class actionHandlerClass;
    private Method actionListingMethod;
    private Map<String, ConfigurationAction> availableActions = new HashMap<String, ConfigurationAction>();
    private ObjectFactory objectFactory;

    public ConfigurationActions(Class configurationClass, Class actionHandlerClass, ObjectFactory objectFactory)
    {
        this.configurationClass = configurationClass;
        this.actionHandlerClass = actionHandlerClass;
        this.objectFactory = objectFactory;
        findActionListingMethod();
        findAvailableActions();
    }

    private void findActionListingMethod()
    {
        if (actionHandlerClass != null)
        {
            Method[] methods = actionHandlerClass.getMethods();
            for (Method method : methods)
            {
                if (method.getName().equals("getActions") &&
                    (ReflectionUtils.acceptsParameters(method) || ReflectionUtils.acceptsParameters(method, configurationClass)) &&
                    ReflectionUtils.returnsParameterisedType(method, List.class, String.class))
                {
                    actionListingMethod = method;
                    break;
                }
            }
        }
    }

    private void findAvailableActions()
    {
        if(actionHandlerClass != null)
        {
            Method[] methods = actionHandlerClass.getMethods();
            for (Method method : methods)
            {
                // Note do is a keyword, so if the name starts with do it must
                // have at least one more character.
                String methodName = method.getName();
                if (!methodName.startsWith("do"))
                {
                    continue;
                }
                if (method.getReturnType() != Void.TYPE)
                {
                    continue;
                }

                int parameterCount = method.getParameterTypes().length;
                if (parameterCount > 2)
                {
                    continue;
                }

                Class argumentType = null;
                if (parameterCount > 0)
                {
                    Class param = method.getParameterTypes()[0];
                    if (!param.isAssignableFrom(configurationClass))
                    {
                        continue;
                    }

                    if (parameterCount == 2)
                    {
                        argumentType = method.getParameterTypes()[1];
                        if(!Configuration.class.isAssignableFrom(argumentType))
                        {
                            continue;
                        }
                    }
                }

                // ok, we have an action here.
                String action = methodToAction(methodName);
                availableActions.put(action, new ConfigurationAction(action, getPermissionName(method), argumentType, method));
            }
        }
    }

    private String methodToAction(String methodName)
    {
        // ok, we have an action here.
        String actionName = methodName.substring(2, 3).toLowerCase();
        if (methodName.length() > 3)
        {
            actionName = actionName + methodName.substring(3);
        }
        return actionName;
    }

    private String getPermissionName(Method method)
    {
        String permissionName = AccessManager.ACTION_WRITE;
        Permission permission = method.getAnnotation(Permission.class);
        if(permission != null)
        {
            permissionName = permission.value();
        }

        return permissionName;
    }

    public Class getConfigurationClass()
    {
        return configurationClass;
    }

    public Class getActionHandlerClass()
    {
        return actionHandlerClass;
    }

    public void addAction(ConfigurationAction action)
    {
        availableActions.put(action.getName(), action);
    }

    public ConfigurationAction getAction(String name)
    {
        return availableActions.get(name);
    }

    public Iterable<ConfigurationAction> getAvailableActions()
    {
        return availableActions.values();
    }

    public boolean hasAction(String name)
    {
        return getAction(name) != null;
    }

    List<ConfigurationAction> getActions(Object configurationInstance) throws Exception
    {
        List<ConfigurationAction> actions;
        if (actionListingMethod == null)
        {
            actions = new LinkedList<ConfigurationAction>(availableActions.values());
        }
        else
        {
            List<String> actionNames = new LinkedList<String>();

            Object actionHandler = objectFactory.buildBean(actionHandlerClass);
            if (actionListingMethod.getParameterTypes().length == 0)
            {
                actionNames = (List<String>) actionListingMethod.invoke(actionHandler);
            }
            else
            {
                actionNames = (List<String>) actionListingMethod.invoke(actionHandler, configurationInstance);
            }

            actions = new LinkedList<ConfigurationAction>();
            for(String actionName: actionNames)
            {
                ConfigurationAction action = availableActions.get(actionName);
                if(action != null)
                {
                    actions.add(action);
                }
                else
                {
                    LOG.warning("Dropping action '" + action + "' from class '" + configurationClass.getName() + "' because no corresponding method was found");
                }
            }
        }

        return actions;
    }

    void execute(String name, Object configurationInstance, Object argument) throws Exception
    {
        ConfigurationAction action = availableActions.get(name);
        if(action == null)
        {
            throw new IllegalArgumentException("Unrecognised action '" + name + "' for type '" + configurationClass.getName() + "'");
        }

        if(!configurationClass.isInstance(configurationInstance))
        {
            throw new IllegalArgumentException("Invoking action '" + name + "': configuration instance is of wrong type: expecting '" + configurationClass.getName() + "', got '" + configurationInstance.getClass().getName() + "'");
        }

        Object handlerInstance = objectFactory.buildBean(actionHandlerClass);
        Class argumentClass = action.getArgumentClass();
        if (argumentClass == null)
        {
            action.getMethod().invoke(handlerInstance, configurationInstance);
        }
        else
        {
            if(argument != null && !argumentClass.isInstance(argument))
            {
                throw new IllegalArgumentException("Invoking action '" + name + "' of type '" + configurationClass.getName() + "': argument instance is of wrong type: expecting '" + argumentClass.getName() + "', got '" + argument.getClass().getName() + "'");
            }

            action.getMethod().invoke(handlerInstance, configurationInstance, argument);
        }
    }
}

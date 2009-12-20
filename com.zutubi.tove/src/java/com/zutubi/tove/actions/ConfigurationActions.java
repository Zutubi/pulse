package com.zutubi.tove.actions;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Permission;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.zutubi.util.reflection.MethodPredicates.*;

/**
 * Describes a bunch of available actions for a configuration type.
 */
public class ConfigurationActions
{
    private static final Logger LOG = Logger.getLogger(ConfigurationActions.class);

    private static final String I18N_KEY_DEFAULT_FEEDBACK = "default.feedback";

    private Class configurationClass;
    private Class<?> actionHandlerClass;
    private Method enabledMethod;
    private Method actionListingMethod;
    private Map<String, ConfigurationAction> availableActions = new HashMap<String, ConfigurationAction>();
    private ObjectFactory objectFactory;

    public ConfigurationActions(Class configurationClass, Class actionHandlerClass, ObjectFactory objectFactory)
    {
        this.configurationClass = configurationClass;
        this.actionHandlerClass = actionHandlerClass;
        this.objectFactory = objectFactory;
        findEnabledMethod();
        findActionListingMethod();
        findAvailableActions();
    }

    private void findEnabledMethod()
    {
        if (actionHandlerClass != null)
        {
            enabledMethod = CollectionUtils.find(actionHandlerClass.getMethods(), new Predicate<Method>()
            {
                public boolean satisfied(Method method)
                {
                    return method.getName().equals("actionsEnabled") &&
                            (ReflectionUtils.acceptsParameters(method, configurationClass, boolean.class)) &&
                            method.getReturnType().equals(boolean.class);
                }
            });
        }
    }

    private void findActionListingMethod()
    {
        if (actionHandlerClass != null)
        {
            actionListingMethod = CollectionUtils.find(actionHandlerClass.getMethods(),
                    and(hasName("getActions"), or(acceptsParameters(), acceptsParameters(configurationClass)), returnsType(List.class, String.class)));
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
                if (method.getReturnType() != Void.TYPE && method.getReturnType() != ActionResult.class)
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
                availableActions.put(action, new ConfigurationAction(action, getPermissionName(method), argumentType, getCustomiseMethod(action), getPrepareMethod(action, argumentType), method));
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

    private Method getCustomiseMethod(final String action)
    {
        return getMethodWithOptionalReturn(action, String.class, "customise");
    }

    private Method getPrepareMethod(final String action, final Class argumentType)
    {
        return getMethodWithOptionalReturn(action, argumentType, "prepare");
    }

    private Method getMethodWithOptionalReturn(String action, final Class optionalReturnType, String prefix)
    {
        final String expectedName = prefix + action.substring(0, 1).toUpperCase() + action.substring(1);
        return CollectionUtils.find(actionHandlerClass.getMethods(), new Predicate<Method>()
        {
            public boolean satisfied(Method method)
            {
                if(!method.getName().equals(expectedName))
                {
                    return false;
                }

                if(!ReflectionUtils.acceptsParameters(method) && !ReflectionUtils.acceptsParameters(method, configurationClass))
                {
                    return false;
                }

                Class<?> returnType = method.getReturnType();
                return  returnType == Void.TYPE || optionalReturnType != null && optionalReturnType.isAssignableFrom(returnType);
            }
        });
    }

    public Class getConfigurationClass()
    {
        return configurationClass;
    }

    public Class getActionHandlerClass()
    {
        return actionHandlerClass;
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

    boolean hasEnabledMethod()
    {
        return enabledMethod != null;
    }
    
    boolean actionsEnabled(Configuration instance, boolean deeplyValid)
    {
        if (enabledMethod != null)
        {
            try
            {
                Object actionHandler = objectFactory.buildBean(actionHandlerClass);
                return (Boolean) enabledMethod.invoke(actionHandler, instance, deeplyValid);
            }
            catch (Exception e)
            {
                LOG.severe(e);
                // Fall through to default behaviour
            }
        }

        // By default only concrete, valid instances may have actions
        return instance.isConcrete() && deeplyValid;
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
            List<String> actionNames;

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
                    LOG.warning("Dropping action '" + actionName + "' from class '" + configurationClass.getName() + "' because no corresponding method was found");
                }
            }
        }

        return actions;
    }

    public String customise(String name, Configuration configurationInstance) throws Exception
    {
        ConfigurationAction action = verifyAction(name, configurationInstance);
        Method customiseMethod = action.getCustomiseMethod();
        String result = null;
        if(customiseMethod != null)
        {
            Object handlerInstance = objectFactory.buildBean(actionHandlerClass);
            if(customiseMethod.getParameterTypes().length == 0)
            {
                result = (String) customiseMethod.invoke(handlerInstance);
            }
            else
            {
                result = (String) customiseMethod.invoke(handlerInstance, configurationInstance);
            }
        }

        return result;
    }

    Configuration prepare(String name, Configuration configurationInstance) throws Exception
    {
        ConfigurationAction action = verifyAction(name, configurationInstance);
        Method prepareMethod = action.getPrepareMethod();
        Configuration result = null;
        if(prepareMethod != null)
        {
            Object handlerInstance = objectFactory.buildBean(actionHandlerClass);
            if(prepareMethod.getParameterTypes().length == 0)
            {
                result = (Configuration) prepareMethod.invoke(handlerInstance);
            }
            else
            {
                result = (Configuration) prepareMethod.invoke(handlerInstance, configurationInstance);
            }
        }

        return result;
    }

    ActionResult execute(String name, Configuration configurationInstance, Configuration argument) throws Exception
    {
        ConfigurationAction action = verifyAction(name, configurationInstance);

        Object handlerInstance = objectFactory.buildBean(actionHandlerClass);
        Class argumentClass = action.getArgumentClass();
        ActionResult result;
        if (argumentClass == null)
        {
            result = (ActionResult) action.getMethod().invoke(handlerInstance, configurationInstance);
        }
        else
        {
            if(argument != null && !argumentClass.isInstance(argument))
            {
                throw new IllegalArgumentException("Invoking action '" + name + "' of type '" + configurationClass.getName() + "': argument instance is of wrong type: expecting '" + argumentClass.getName() + "', got '" + argument.getClass().getName() + "'");
            }

            result = (ActionResult) action.getMethod().invoke(handlerInstance, configurationInstance, argument);
        }

        if (result == null)
        {
            result = getDefaultResult(name);
        }

        if (result.getMessage() == null)
        {
            result = new ActionResult(result.getStatus(), getDefaultFeedback(name), result.getInvalidatedPaths());
        }

        return result;
    }

    private ActionResult getDefaultResult(String name)
    {
        return new ActionResult(ActionResult.Status.SUCCESS, getDefaultFeedback(name));
    }

    private String getDefaultFeedback(String name)
    {
        String feedback;
        Messages messages = Messages.getInstance(configurationClass);
        String key = name + ActionManager.I18N_KEY_SUFFIX_FEEDACK;

        if (messages.isKeyDefined(key))
        {
            feedback = messages.format(key, name);
        }
        else
        {
            String actionLabel = messages.format(name + ConventionSupport.I18N_KEY_SUFFIX_LABEL);
            if (actionLabel == null)
            {
                actionLabel = name;
            }

            messages = Messages.getInstance(ConfigurationActions.class);
            feedback = messages.format(I18N_KEY_DEFAULT_FEEDBACK, actionLabel);
        }
        return feedback;
    }

    private ConfigurationAction verifyAction(String name, Configuration configurationInstance)
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
        return action;
    }
}

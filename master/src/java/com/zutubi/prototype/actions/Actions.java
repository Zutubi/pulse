package com.zutubi.prototype.actions;

import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
//FIXME: rename to something more appropriate. 
public class Actions
{
    private static final Logger LOG = Logger.getLogger(Actions.class);

    private ObjectFactory objectFactory;

    @SuppressWarnings({"unchecked"})
    public List<String> getActions(Class actionHandlerClass, Object configurationInstance)
    {
        // is the getActions method defined?
        Method[] methods = actionHandlerClass.getMethods();
        for (Method method : methods)
        {
            if (method.getName().equals("getActions") && method.getReturnType().isAssignableFrom(List.class))
            {
                // ok, we want to execute this method
                try
                {
                    Object instance = objectFactory.buildBean(actionHandlerClass);
                    switch (method.getParameterTypes().length)
                    {
                        case 0:
                            return (List<String>) method.invoke(instance);
                        case 1:
                            return (List<String>) method.invoke(instance, configurationInstance);
                    }
                }
                catch (Exception e)
                {
                    LOG.debug(e);
                }
            }
        }

        // if not, extract all of the doXXX methods and return the XXX as the action names.
        return getDefaultActions(actionHandlerClass, configurationInstance.getClass());
    }

    /**
     * Get the list of actions based on the action handler methods.  That is, all of the do<ActionName>() methods.
     *
     * @param actionHandlerClass
     * @param configurationClass
     *
     * @return a list of action names.
     */
    //TODO: configurationClass type should be determinable from the actionHandlerClass type via parametrization
    public List<String> getDefaultActions(Class actionHandlerClass, Class configurationClass)
    {
        if (configurationClass == null)
        {
            configurationClass = Object.class;
        }

        Method[] methods = actionHandlerClass.getMethods();
        List<String> actions = new LinkedList<String>();
        for (Method method : methods)
        {
            String methodName = method.getName();
            if (!methodName.startsWith("do") && methodName.length() > 2)
            {
                continue;
            }
            if (method.getReturnType() != Void.TYPE)
            {
                continue;
            }
            if (method.getParameterTypes().length > 1)
            {
                continue;
            }
            if (method.getParameterTypes().length == 1)
            {
                Class param = method.getParameterTypes()[0];
                if (!param.isAssignableFrom(configurationClass))
                {
                    continue;
                }
            }

            // ok, we have an action here.
            actions.add(methodToAction(methodName));
        }
        return actions;
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

    @SuppressWarnings({"unchecked"})
    public void execute(Class actionHandlerClass, String actionName, Object configurationInstance)
    {
        String methodName = actionToMethod(actionName);

        try
        {
            for (Method method : actionHandlerClass.getMethods())
            {
                if (!method.getName().equals(methodName))
                {
                    continue;
                }

                if (method.getReturnType() != Void.TYPE)
                {
                    continue;
                }
                if (method.getParameterTypes().length > 1)
                {
                    continue;
                }

                if (method.getParameterTypes().length == 1)
                {
                    Class param = method.getParameterTypes()[0];
                    if (!param.isAssignableFrom(configurationInstance.getClass()))
                    {
                        continue;
                    }
                    Object instance = objectFactory.buildBean(actionHandlerClass);
                    method.invoke(instance, configurationInstance);
                    return;
                }
                else
                {
                    Object instance = objectFactory.buildBean(actionHandlerClass);
                    method.invoke(instance);
                    return;
                }
            }
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Invalid action name: " + actionName);
        }
        catch (Exception e)
        {
            LOG.debug(e);
        }
    }

    private String actionToMethod(String actionName)
    {
        String methodName = "do" + actionName.substring(0, 1).toUpperCase();
        if (actionName.length() > 1)
        {
            methodName = methodName + actionName.substring(1);
        }
        return methodName;
    }

    /**
     * Required resource
     *
     * @param objectFactory instance
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}

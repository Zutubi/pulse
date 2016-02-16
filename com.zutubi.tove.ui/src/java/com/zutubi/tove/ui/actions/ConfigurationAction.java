package com.zutubi.tove.ui.actions;

import java.lang.reflect.Method;

/**
 * Describes an action that can be performed on a configuration object.  The
 * action is executed using the &lt;configuration type&gt;Actions class.
 */
public class ConfigurationAction
{
    /**
     * Name of the action, must be unique for the type it is executing on.
     */
    private String name;
    /**
     * Name of the permission needed to execute this action.  By default, the
     * action name itself is used.  However, related actions can be grouped
     * under one permission (e.g. the "pause" permission is used for both
     * "pause" and "resume").
     */
    private String permissionName;
    /**
     * The type of the argument passed to the action.  If null, the action
     * accepts no argument.
     */
    private Class argumentClass;
    /**
     * Optional method to call to determine if the action has multiple variants
     * that can be presented in a graphical UI.
     */
    private Method variantsMethod;
    /**
     * Optional method to call before executing this action: e.g. to
     * prepare a default argument.
     */
    private Method prepareMethod;
    /**
     * The do... method used to execute this action.
     */
    private Method method;

    public ConfigurationAction(String name, Method method)
    {
        this.name = name;
        this.method = method;
    }

    public ConfigurationAction(String name, String permissionName, Class argumentClass, Method variantsMethod, Method prepareMethod, Method method)
    {
        this.name = name;
        this.permissionName = permissionName;
        this.argumentClass = argumentClass;
        this.variantsMethod = variantsMethod;
        this.prepareMethod = prepareMethod;
        this.method = method;
    }

    public String getName()
    {
        return name;
    }

    public String getPermissionName()
    {
        return permissionName;
    }

    public Class getArgumentClass()
    {
        return argumentClass;
    }

    public boolean hasArgument()
    {
        return argumentClass != null;
    }

    public Method getVariantsMethod()
    {
        return variantsMethod;
    }

    public boolean hasVariants()
    {
        return variantsMethod != null;
    }

    public Method getPrepareMethod()
    {
        return prepareMethod;
    }

    public Method getMethod()
    {
        return method;
    }
}

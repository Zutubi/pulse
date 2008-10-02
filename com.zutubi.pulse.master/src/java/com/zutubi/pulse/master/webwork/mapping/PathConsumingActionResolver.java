package com.zutubi.pulse.master.webwork.mapping;

import com.zutubi.util.TextUtils;

/**
 * A resolver that allows an arbitrary path to be appended to the url.  The
 * path is captured as a single parameter.
 */
public class PathConsumingActionResolver extends ActionResolverSupport
{
    private String paramName;
    private String currentPath;

    public PathConsumingActionResolver(String action, String paramName)
    {
        super(action);
        this.paramName = paramName;
    }

    public PathConsumingActionResolver(String action, String paramName, String currentPath)
    {
        super(action);
        this.paramName = paramName;
        this.currentPath = currentPath;
        addParameter(paramName, currentPath);
    }

    public ActionResolver getChild(String name)
    {
        String path = name;
        if(TextUtils.stringSet(currentPath))
        {
            path = currentPath + "/" + path;
        }
        
        return new PathConsumingActionResolver(getAction(), paramName, path);
    }
}

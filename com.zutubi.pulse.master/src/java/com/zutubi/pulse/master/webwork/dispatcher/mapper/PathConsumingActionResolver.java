package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;

import java.util.Arrays;
import java.util.List;

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

    public List<String> listChildren()
    {
        return Arrays.asList("<path element>");
    }

    public ActionResolver getChild(String name)
    {
        String path = StringUtils.uriComponentEncode(name);
        if(TextUtils.stringSet(currentPath))
        {
            path = currentPath + "/" + path;
        }
        
        return new PathConsumingActionResolver(getAction(), paramName, path);
    }
}

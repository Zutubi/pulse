package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;
import com.zutubi.pulse.webwork.mapping.ParamNameActionResolver;

/**
 */
public class BuildChangesActionResolver extends ActionResolverSupport
{
    public BuildChangesActionResolver()
    {
        super("viewChanges");
    }

    public ActionResolver getChild(String name)
    {
        if(name.equals("sinceBuild"))
        {
            return new ParamNameActionResolver(getAction(), name);
        }
        else
        {
            return new ChangelistActionResolver(name);
        }
    }
}

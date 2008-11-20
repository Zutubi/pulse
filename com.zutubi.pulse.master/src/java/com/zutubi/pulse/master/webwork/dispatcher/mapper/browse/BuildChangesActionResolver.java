package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParamNameActionResolver;

import java.util.Arrays;
import java.util.List;

/**
 */
public class BuildChangesActionResolver extends ActionResolverSupport
{
    public BuildChangesActionResolver()
    {
        super("viewChanges");
    }

    public List<String> listChildren()
    {
        return Arrays.asList("sinceBuild", "<changelist id>");
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

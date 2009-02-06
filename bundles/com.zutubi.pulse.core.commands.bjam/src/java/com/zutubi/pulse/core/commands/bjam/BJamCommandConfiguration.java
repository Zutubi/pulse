package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.TextUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.bjamCommandConfig")
public class BJamCommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String jamfile;
    private String targets;

    public BJamCommandConfiguration()
    {
        super(BJamCommand.class, "bjam.bin", "bjam");
    }

    public String getJamfile()
    {
        return jamfile;
    }

    public void setJamfile(String jamfile)
    {
        this.jamfile = jamfile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();

        if (TextUtils.stringSet(jamfile))
        {
            result.add(new NamedArgument("jamfile", jamfile, "-f"));
        }

        if (TextUtils.stringSet(targets))
        {
            result.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }

        return result;
    }
}

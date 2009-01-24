package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SystemUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.antCommandConfig")
public class AntCommandConfiguration extends NamedArgumentCommandConfigurationSupport
{
    private String buildFile;
    private String targets;

    public AntCommandConfiguration()
    {
        super(AntCommand.class, "ant.bin", SystemUtils.IS_WINDOWS ? "ant.bat" : "ant");
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
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
        List<NamedArgument> args = new LinkedList<NamedArgument>();
        if (buildFile != null)
        {
            args.add(new NamedArgument("build file", buildFile, "-f"));
        }

        if (targets != null)
        {
            args.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }

        return args;
    }
}

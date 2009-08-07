package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link AntCommand}.
 */
@SymbolicName("zutubi.antCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "buildFile", "targets", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class AntCommandConfiguration extends NamedArgumentCommandConfiguration
{
    @BrowseScmFileAction(baseDirField = "workingDir")
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
        if (StringUtils.stringSet(buildFile))
        {
            args.add(new NamedArgument("build file", buildFile, "-f"));
        }

        if (StringUtils.stringSet(targets))
        {
            args.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }

        return args;
    }
}

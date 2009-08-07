package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link MakeCommand}.
 */
@SymbolicName("zutubi.makeCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "makefile", "targets", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class MakeCommandConfiguration extends NamedArgumentCommandConfiguration
{
    @BrowseScmFileAction(baseDirField = "workingDir")
    private String makefile;
    private String targets;

    public MakeCommandConfiguration()
    {
        super(MakeCommand.class, "make.bin", "make");
    }

    public String getMakefile()
    {
        return makefile;
    }

    public void setMakefile(String makefile)
    {
        this.makefile = makefile;
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

        if (StringUtils.stringSet(makefile))
        {
            result.add(new NamedArgument("makefile", makefile, "-f"));
        }

        if (StringUtils.stringSet(targets))
        {
            result.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }

        return result;
    }
}

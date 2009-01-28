package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.TextUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.makeCommandConfig")
public class MakeCommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String makefile;
    private String targets;

    public MakeCommandConfiguration()
    {
        super(NamedArgumentCommand.class, "make.bin", "make");
        getPostProcessors().add(new MakePostProcessorConfiguration());
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

        if (TextUtils.stringSet(makefile))
        {
            result.add(new NamedArgument("makefile", makefile, "-f"));
        }

        if (TextUtils.stringSet(targets))
        {
            result.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }

        return result;
    }
}

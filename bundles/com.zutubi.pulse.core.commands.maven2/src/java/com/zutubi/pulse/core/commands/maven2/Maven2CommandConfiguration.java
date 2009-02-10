package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.TextUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link Maven2Command}.
 */
@SymbolicName("zutubi.maven2CommandConfig")
@Form(fieldOrder = {"name", "workingDir", "goals", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class Maven2CommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String goals;

    public Maven2CommandConfiguration()
    {
        super(Maven2Command.class, "maven2.bin", SystemUtils.IS_WINDOWS ? "mvn.bat" : "mvn");
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();
        
        if (TextUtils.stringSet(goals))
        {
            result.add(new NamedArgument("goals", goals, Arrays.asList(goals.trim().split("\\s+"))));
        }

        return result;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals(String goals)
    {
        this.goals = goals;
    }
}

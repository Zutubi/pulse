package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.TextUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The Maven2 command is used to run a maven 2 build.
 */
public class Maven2CommandConfiguration extends NamedArgumentCommandConfiguration
{
    /**
     * The goals to be passed to the maven2 command line.  The format is a space
     * separated list of goals.
     */
    private String goals;

    public Maven2CommandConfiguration()
    {
        super(Maven2Command.class, "maven2.bin", SystemUtils.IS_WINDOWS ? "mvn.bat" : "mvn");
        getPostProcessors().add(new Maven2PostProcessorConfiguration("maven2.pp"));
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

package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.util.SystemUtils;

import java.io.File;

/**
 * The Maven2 command is used to run a maven 2 build.
 */
public class Maven2Command extends ExecutableCommand
{
    /**
     * The goals to be passed to the maven2 command line.  The format is a space
     * separated list of goals.
     */
    private String goals;
    /**
     * The default maven 2 post processor used for all maven2 command output processing.
     */
    private Maven2PostProcessor pp = new Maven2PostProcessor("maven2.pp");

    public Maven2Command()
    {
        super("maven2.bin", SystemUtils.IS_WINDOWS ? "mvn.bat" : "mvn");
    }

    public void execute(ExecutionContext context, CommandResult cmdResult)
    {
        if (goals != null)
        {
            addArguments(goals.trim().split(" +"));
            cmdResult.getProperties().put("goals", goals);
        }

        ProcessArtifact pa = createProcess();
        pa.setProcessor(pp);

        super.execute(context, cmdResult);

        try
        {
            //TODO: use the context's variables to transfer this maven specific information around. 
            PulseExecutionContext pec = (PulseExecutionContext) context;
            pec.setVersion(MavenUtils.extractVersion(new File(getWorkingDir(context.getWorkingDir()), "pom.xml"), "version"));
        }
        catch (PulseException e)
        {
            cmdResult.warning(e.getMessage());
        }
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals(String goals)
    {
        this.goals = goals;
    }

    public ExpressionElement createSuppressWarning()
    {
        ExpressionElement element = new ExpressionElement();
        pp.addSuppressWarning(element);
        return element;
    }

    public ExpressionElement createSuppressError()
    {
        ExpressionElement element = new ExpressionElement();
        pp.addSuppressError(element);
        return element;
    }

    public Maven2PostProcessor getPp()
    {
        return pp;
    }
}

package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.util.SystemUtils;

import java.io.File;

/**
 */
public class Maven2Command extends ExecutableCommand
{
    private String goals;
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
            context.setVersion(MavenUtils.extractVersion(new File(getWorkingDir(context.getWorkingDir()), "pom.xml"), "version"));
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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.util.SystemUtils;

import java.io.File;

/**
 * <class-comment/>
 */
public class Maven2Command extends ExecutableCommand
{
    private String goals;
    private Maven2PostProcessor pp = new Maven2PostProcessor("maven2.pp");

    private void checkExe()
    {
        if (getExe() == null)
        {
            Scope scope = getScope();
            if (scope != null)
            {
                Reference ref = scope.getReference("maven2.bin");
                if (ref != null && ref.getValue() instanceof String)
                {
                    setExe((String) ref.getValue());
                }
            }

            // use defaults.
            if (getExe() == null)
            {
                if (SystemUtils.IS_WINDOWS)
                {
                    setExe("mvn.bat");
                }
                else
                {
                    setExe("mvn");
                }
            }
        }
    }

    public void execute(CommandContext context, CommandResult cmdResult)
    {
        checkExe();

        if (goals != null)
        {
            addArguments(goals.trim().split(" +"));
            cmdResult.getProperties().put("goals", goals);
        }

        super.execute(context, cmdResult);

        MavenUtils.extractVersion(context, cmdResult, new File(getWorkingDir(context.getPaths()), "pom.xml"), "version");

        StoredArtifact artifact = cmdResult.getArtifact(OUTPUT_ARTIFACT_NAME);
        if(artifact != null)
        {
            pp.process(artifact.getFile(), cmdResult, context);
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

    public void setScope(Scope scope)
    {
        super.setScope(scope);
        checkExe();
    }
}

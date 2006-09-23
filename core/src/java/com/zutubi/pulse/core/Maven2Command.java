package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.SystemUtils;

/**
 * <class-comment/>
 */
public class Maven2Command extends ExecutableCommand
{
    private String goals;

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
                if (SystemUtils.isWindows())
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

    public void execute(long recipeId, CommandContext context, CommandResult cmdResult)
    {
        checkExe();

        if (goals != null)
        {
            addArguments(goals.trim().split(" +"));
            cmdResult.getProperties().put("goals", goals);
        }

        super.execute(recipeId, context, cmdResult);

        Maven2PostProcessor pp = new Maven2PostProcessor("maven.pp");
        pp.process(cmdResult.getArtifact(OUTPUT_NAME).getFile(), cmdResult, context);
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals(String goals)
    {
        this.goals = goals;
    }

    public void setScope(Scope scope)
    {
        super.setScope(scope);
        checkExe();
    }
}

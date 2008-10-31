package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.pulse.master.tove.config.project.BrowseScmDirAction;
import com.zutubi.util.TextUtils;
import org.apache.velocity.VelocityContext;

/**
 *
 *
 */
@SymbolicName("zutubi.maven2TypeConfig")
@Form(fieldOrder = {"workingDir", "goals", "arguments", "postProcessors"})
public class Maven2TypeConfiguration extends TemplateTypeConfiguration
{
    private String goals;
    @BrowseScmDirAction
    private String workingDir;
    private String arguments;

    public String getGoals()
    {
        return goals;
    }

    public void setGoals(String goals)
    {
        this.goals = goals;
    }

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }
    
    protected String getTemplateName()
    {
        return "maven2.template.vm";
    }

    protected void setupContext(VelocityContext context)
    {
        if (TextUtils.stringSet(goals))
        {
            context.put("goals", goals);
        }
        if (TextUtils.stringSet(workingDir))
        {
            context.put("workingDir", workingDir.trim());
        }

        if (TextUtils.stringSet(arguments))
        {
            context.put("arguments", arguments);
        }
    }
}

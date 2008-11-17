package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.master.tove.config.project.BrowseScmDirAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.ValidRegex;
import org.apache.velocity.VelocityContext;

/**
 * The UI configuration for the maven 2 project template.
 */
@SymbolicName("zutubi.maven2TypeConfig")
@Form(fieldOrder = {"workingDir", "goals", "arguments", "postProcessors", "suppressWarning", "suppressError"})
public class Maven2TypeConfiguration extends TemplateTypeConfiguration
{
    private String goals;
    @BrowseScmDirAction
    private String workingDir;
    private String arguments;
    @Wizard.Ignore
    @ValidRegex
    private String suppressError;
    @Wizard.Ignore
    @ValidRegex
    private String suppressWarning;
    
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

    public String getSuppressError()
    {
        return suppressError;
    }

    public void setSuppressError(String suppressError)
    {
        this.suppressError = suppressError;
    }

    public String getSuppressWarning()
    {
        return suppressWarning;
    }

    public void setSuppressWarning(String suppressWarning)
    {
        this.suppressWarning = suppressWarning;
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
        if (TextUtils.stringSet(suppressWarning))
        {
            context.put("suppressWarning", suppressWarning);
        }
        if (TextUtils.stringSet(suppressError))
        {
            context.put("suppressError", suppressError);
        }
    }
}

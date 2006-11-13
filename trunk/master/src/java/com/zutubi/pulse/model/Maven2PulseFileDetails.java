package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class Maven2PulseFileDetails extends TemplatePulseFileDetails
{
    /**
     * Space-separated list of goal names (persists more efficiently)
     */
    private String goals;
    private String workingDir;
    private String arguments;

    public Maven2PulseFileDetails()
    {

    }

    public Maven2PulseFileDetails(String goals, String workingDir, String arguments)
    {
        this.goals = goals;
        this.workingDir = workingDir;
        this.arguments = arguments;
    }

    public Maven2PulseFileDetails copy()
    {
        return new Maven2PulseFileDetails(goals, workingDir, arguments);
    }

    protected String getTemplateName()
    {
        return "maven2.template.vm";
    }

    protected void populateContext(VelocityContext context)
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

    public String getType()
    {
        return "maven2";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();

        if (TextUtils.stringSet(goals))
        {
            result.put("goals", goals);
        }

        if (TextUtils.stringSet(workingDir))
        {
            result.put("working directory", workingDir);
        }

        if (TextUtils.stringSet(arguments))
        {
            result.put("arguments", arguments);
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
}

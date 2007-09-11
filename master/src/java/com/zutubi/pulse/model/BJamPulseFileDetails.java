package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.Properties;

/**
 */
public class BJamPulseFileDetails extends TemplatePulseFileDetails
{
    private String jamfile;
    /**
     * Space-separated list of target names (persists more efficiently)
     */
    private String targets;
    /**
     * Extra arguments to pass to make.
     */
    private String arguments;
    /**
     * Path relative to work.dir in which to execute bjam.
     */
    private String workingDir;

    public BJamPulseFileDetails()
    {
        jamfile = null;
        targets = null;
        arguments = null;
        workingDir = null;
    }

    public BJamPulseFileDetails(String buildFile, String targets, String arguments, String workingDir)
    {
        this.jamfile = buildFile;
        this.targets = targets;
        this.arguments = arguments;
        this.workingDir = workingDir;
    }

    public BJamPulseFileDetails copy()
    {
        BJamPulseFileDetails copy = new BJamPulseFileDetails(jamfile, targets, arguments, workingDir);
        copyCommon(copy);
        return copy;
    }

    protected String getTemplateName()
    {
        return "bjam.template.vm";
    }

    protected void populateContext(VelocityContext context)
    {
        if (TextUtils.stringSet(jamfile))
        {
            context.put("jamfile", jamfile);
        }

        if (TextUtils.stringSet(targets))
        {
            context.put("targets", targets);
        }

        if (TextUtils.stringSet(arguments))
        {
            context.put("arguments", arguments);
        }

        if (TextUtils.stringSet(workingDir))
        {
            context.put("workingDir", workingDir);
        }
    }

    public String getJamfile()
    {
        return jamfile;
    }

    public void setJamfile(String jamfile)
    {
        this.jamfile = jamfile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getType()
    {
        return "bjam";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();

        if (TextUtils.stringSet(jamfile))
        {
            result.put("Jamfile", jamfile);
        }

        if (TextUtils.stringSet(targets))
        {
            result.put("targets", targets);
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
}

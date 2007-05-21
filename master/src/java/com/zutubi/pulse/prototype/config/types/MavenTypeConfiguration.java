package com.zutubi.pulse.prototype.config.types;

import com.zutubi.config.annotations.SymbolicName;
import com.opensymphony.util.TextUtils;
import org.apache.velocity.VelocityContext;

/**
 *
 *
 */
@SymbolicName("internal.mavenTypeConfig")
public class MavenTypeConfiguration extends TemplateTypeConfiguration
{
    private String targets;

    private String workingDir;
    
    private String arguments;

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
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
        return "maven.template.vm";
    }

    protected void setupContext(VelocityContext context)
    {
        if (TextUtils.stringSet(targets))
        {
            context.put("targets", targets);
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

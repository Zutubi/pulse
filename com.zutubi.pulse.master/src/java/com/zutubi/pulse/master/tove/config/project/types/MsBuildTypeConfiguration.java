package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.pulse.master.tove.config.project.BrowseScmDirAction;
import com.zutubi.pulse.master.tove.config.project.BrowseScmFileAction;
import com.zutubi.util.TextUtils;
import org.apache.velocity.VelocityContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Project type for MsBuild projects.  A small wrapper around MsBuildCommand.
 */
@SymbolicName("zutubi.msbuildTypeConfig")
@Form(fieldOrder = {"workingDirectory", "buildFile", "targets", "configuration", "arguments"})
public class MsBuildTypeConfiguration extends TemplateTypeConfiguration
{
    @BrowseScmDirAction
    private String workingDirectory;
    @BrowseScmFileAction(baseDirField = "workingDirectory")
    private String buildFile;
    private String targets;
    private String configuration;
    private String arguments;
    private Map<String, MsBuildPropertyConfiguration> buildProperties = new HashMap<String, MsBuildPropertyConfiguration>();

    public String getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public String getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(String configuration)
    {
        this.configuration = configuration;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }

    public Map<String, MsBuildPropertyConfiguration> getBuildProperties()
    {
        return buildProperties;
    }

    public void setBuildProperties(Map<String, MsBuildPropertyConfiguration> buildProperties)
    {
        this.buildProperties = buildProperties;
    }

    protected void setupContext(VelocityContext context)
    {
        if (TextUtils.stringSet(workingDirectory))
        {
            context.put("workingDirectory", workingDirectory);
        }

        if (TextUtils.stringSet(buildFile))
        {
            context.put("buildFile", buildFile);
        }

        if (TextUtils.stringSet(targets))
        {
            context.put("targets", targets);
        }

        if (TextUtils.stringSet(configuration))
        {
            context.put("configuration", configuration);
        }

        if (TextUtils.stringSet(arguments))
        {
            context.put("arguments", arguments);
        }

        context.put("buildProperties", buildProperties.values());
    }

    protected String getTemplateName()
    {
        return "msbuild.template.vm";
    }
}

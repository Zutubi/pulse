package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.util.TextUtils;
import org.apache.velocity.VelocityContext;

/**
 * A project that is built by running a single arbitrary command.
 */
@SymbolicName("zutubi.executableTypeConfig")
@Form(fieldOrder = {"workingDir", "executable", "arguments", "postProcessors"})
public class ExecutableTypeConfiguration extends TemplateTypeConfiguration
{
    /**
     * The command to execute.
     */
    private String executable;
    /**
     * Space-separated list of arguments to pass (StringUtils.split format).
     */
    private String arguments;
    /**
     * Path relative to base.dir in which to execute the make.
     */
    private String workingDir;

//    private Map<String, String> environment = new TreeMap<String, String>();

    public String getExecutable()
    {
        return executable;
    }

    public void setExecutable(String executable)
    {
        this.executable = executable;
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

/*
    public Map<String, String> getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment)
    {
        this.environment = environment;
    }
*/

    protected String getTemplateName()
    {
        return "executable.template.vm";
    }

    protected void setupContext(VelocityContext context)
    {
        if (TextUtils.stringSet(executable))
        {
            context.put("executable", executable);
        }

        if (TextUtils.stringSet(arguments))
        {
            context.put("arguments", arguments);
        }

        if (TextUtils.stringSet(workingDir))
        {
            context.put("workingDir", workingDir);
        }

//        context.put("environment", environment);
    }
}

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.OutputProducingCommandConfigurationSupport;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.executableCommandConfig")
public class ExecutableCommandConfiguration extends OutputProducingCommandConfigurationSupport implements Validateable
{
    @Transient
    private String exeProperty;
    @Transient
    private String defaultExe;

    private String exe;
    private String args;
    @Addable("arg")
    private List<String> extraArguments = new LinkedList<String>();
    private File workingDir;
    private String inputFile;
    private List<EnvironmentConfiguration> environments = new LinkedList<EnvironmentConfiguration>();

    private List<StatusMappingConfiguration> statusMappings = new LinkedList<StatusMappingConfiguration>();

    public ExecutableCommandConfiguration()
    {
        super(ExecutableCommand.class);
    }

    protected ExecutableCommandConfiguration(Class<? extends ExecutableCommand> clazz)
    {
        super(clazz);
    }

    protected ExecutableCommandConfiguration(Class<? extends ExecutableCommand> clazz, String exeProperty, String defaultExe)
    {
        super(clazz);
        this.exeProperty = exeProperty;
        this.defaultExe = defaultExe;
    }

    public String getExeProperty()
    {
        return exeProperty;
    }

    public String getDefaultExe()
    {
        return defaultExe;
    }

    public String getExe()
    {
        return exe;
    }

    public void setExe(String exe)
    {
        this.exe = exe;
    }

    public String getArgs()
    {
        return args;
    }

    public void setArgs(String args)
    {
        this.args = args;
    }

    public List<String> getExtraArguments()
    {
        return extraArguments;
    }

    public void setExtraArguments(List<String> extraArguments)
    {
        this.extraArguments = extraArguments;
    }

    @Transient
    public List<String> getCombinedArguments()
    {
        List<String> combined = new LinkedList<String>();
        if (TextUtils.stringSet(args))
        {
            combined.addAll(Arrays.asList(args.split("\\s+")));
        }
        
        combined.addAll(extraArguments);
        return CollectionUtils.filter(combined, new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return TextUtils.stringSet(s);
            }
        });
    }

    public File getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(File workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getInputFile()
    {
        return inputFile;
    }

    public void setInputFile(String inputFile)
    {
        this.inputFile = inputFile;
    }

    public List<EnvironmentConfiguration> getEnvironments()
    {
        return environments;
    }

    public void setEnvironments(List<EnvironmentConfiguration> environments)
    {
        this.environments = environments;
    }

    public List<StatusMappingConfiguration> getStatusMappings()
    {
        return statusMappings;
    }

    public void setStatusMappings(List<StatusMappingConfiguration> statusMappings)
    {
        this.statusMappings = statusMappings;
    }

    public void validate(ValidationContext context)
    {
        if (!TextUtils.stringSet(exe) && !TextUtils.stringSet(getDefaultExe()))
        {
            context.addFieldError("exe", "exe is required");
        }
    }

    @SymbolicName("zutubi.executableCommandConfig.environmentConfig")
    public static class EnvironmentConfiguration extends AbstractConfiguration
    {
        private String name;
        private String value;

        public EnvironmentConfiguration()
        {
        }

        public EnvironmentConfiguration(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
}

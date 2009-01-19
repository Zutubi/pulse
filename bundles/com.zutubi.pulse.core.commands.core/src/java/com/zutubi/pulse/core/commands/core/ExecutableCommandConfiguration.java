package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.PrecapturedArtifact;
import com.zutubi.pulse.core.ProcessArtifact;
import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.engine.AbstractCommandConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.io.File;

/**
 */
@SymbolicName("zutubi.executableCommandConfig")
public class ExecutableCommandConfiguration extends AbstractCommandConfiguration
{
    private String exe;
    private String exeProperty;
    private String defaultExe;
    private String arguments;
    private File workingDir;
    private String inputFile;
    private String outputFile;
    private List<EnvironmentConfiguration> environments = new LinkedList<EnvironmentConfiguration>();

    private PrecapturedArtifact outputArtifact;
    private PrecapturedArtifact envArtifact;

    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();
    private List<String> suppressedEnvironment = new LinkedList<String>(Arrays.asList(System.getProperty("pulse.suppressed.environment.variables", "P4PASSWD PULSE_TEST_SUPPRESSED").split(" +")));
    private List<StatusMapping> statusMappings = new LinkedList<StatusMapping>();

    public String getExe()
    {
        return exe;
    }

    public void setExe(String exe)
    {
        this.exe = exe;
    }

    public String getExeProperty()
    {
        return exeProperty;
    }

    public void setExeProperty(String exeProperty)
    {
        this.exeProperty = exeProperty;
    }

    public String getDefaultExe()
    {
        return defaultExe;
    }

    public void setDefaultExe(String defaultExe)
    {
        this.defaultExe = defaultExe;
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
        return workingDir.getPath();
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = new File(workingDir);
    }

    public String getInputFile()
    {
        return inputFile;
    }

    public void setInputFile(String inputFile)
    {
        this.inputFile = inputFile;
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public void setOutputFile(String outputFile)
    {
        this.outputFile = outputFile;
    }

    public List<EnvironmentConfiguration> getEnvironments()
    {
        return environments;
    }

    public void setEnvironments(List<EnvironmentConfiguration> environments)
    {
        this.environments = environments;
    }

    public List<String> getSuppressedEnvironment()
    {
        return suppressedEnvironment;
    }

    public void setSuppressedEnvironment(List<String> suppressedEnvironment)
    {
        this.suppressedEnvironment = suppressedEnvironment;
    }

    public Command createCommand()
    {
        ExecutableCommand command = new ExecutableCommand();
        command.setExe(exe);
        command.setWorkingDir(workingDir);
        return command;
    }

    @SymbolicName("zutubi.executableCommandConfig.environmentConfig")
    public static class EnvironmentConfiguration
    {
        private String name;
        private String value;

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

package com.zutubi.pulse.model;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * A post build action to run an executable.
 */
public class RunExecutablePostBuildAction extends PostBuildAction
{
    private String command;
    private String arguments;

    public RunExecutablePostBuildAction()
    {
    }

    public RunExecutablePostBuildAction(String name, List<BuildSpecification> specifications, List<ResultState> states, boolean failOnError, String command, String arguments)
    {
        super(name, specifications, states, failOnError);
        this.command = command;
        this.arguments = arguments;
    }

    protected void internalExecute(BuildResult result)
    {
        try
        {
            List<String> commandLine = new LinkedList<String>();
            commandLine.add(command);
            addArguments(commandLine, result);

            ProcessBuilder builder = new ProcessBuilder(commandLine);
            Process child = builder.start();
            int code = child.waitFor();
            if(code != 0)
            {
                addError("Command exited with non-zero exit code (" + code + ")");
            }
        }
        catch (Exception e)
        {
            addError(e.getMessage());
        }
    }

    public String getType()
    {
        return "run executable";
    }

    private void addArguments(List<String> commandLine, BuildResult result) throws FileLoadException
    {
        List<String> args = StringUtils.split(arguments);

        Scope scope = new Scope();
        scope.add(new Property("project", result.getProject().getName()));
        scope.add(new Property("number", Long.toString(result.getNumber())));
        scope.add(new Property("status", result.getState().getString()));

        for(String arg: args)
        {
            commandLine.add(VariableHelper.replaceVariables(arg, true, scope));
        }
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public String getArguments()
    {
        return arguments;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }

    public static void validateArguments(String arguments) throws Exception
    {
        List<String> args = StringUtils.split(arguments);

        Scope scope = new Scope();
        scope.add(new Property("project", "project"));
        scope.add(new Property("number", "number"));
        scope.add(new Property("status", "status"));

        for(String arg: args)
        {
            VariableHelper.replaceVariables(arg, true, scope);
        }
    }
}

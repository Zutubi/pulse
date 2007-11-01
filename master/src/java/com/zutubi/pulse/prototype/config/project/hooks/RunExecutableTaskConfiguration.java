package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.util.SystemUtils;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Form;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 * Run executable tasks are used to execute an arbitrary command in a hook.
 */
@SymbolicName("zutubi.runExecutableTaskConfig")
@Form(fieldOrder = {"command", "arguments"})
public class RunExecutableTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
    @Required
    private String command;
    private String arguments;

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

    public boolean execute(BuildHookContext context)
    {
        try
        {
            List<String> resolvedArguments = VariableHelper.splitAndReplaceVariables(arguments, context.getScope(), true);
            return true;
        }
        catch (FileLoadException e)
        {
            // FIXME
            e.printStackTrace();
            return false;
        }
    }
}

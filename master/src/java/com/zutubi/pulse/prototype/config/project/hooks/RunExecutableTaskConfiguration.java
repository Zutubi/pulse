package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.util.process.AsyncProcess;
import com.zutubi.pulse.util.process.ByteHandler;
import com.zutubi.pulse.util.process.ForwardingByteHandler;
import com.zutubi.pulse.util.process.NullByteHandler;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Run executable tasks are used to execute an arbitrary command in a hook.
 */
@SymbolicName("zutubi.runExecutableTaskConfig")
@Form(fieldOrder = {"command", "arguments", "timeoutApplied", "timeout"})
public class RunExecutableTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
    @Required
    private String command;
    private String arguments;
    @ControllingCheckbox(dependentFields = {"timeout"})
    private boolean timeoutApplied;
    @Numeric(min = 0)
    private int timeout;

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

    public boolean isTimeoutApplied()
    {
        return timeoutApplied;
    }

    public void setTimeoutApplied(boolean timeoutApplied)
    {
        this.timeoutApplied = timeoutApplied;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public void execute(ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode) throws Exception
    {
        AsyncProcess asyncProcess = null;
        try
        {
            Scope scope = context.asScope();
            List<String> resolvedArguments = VariableHelper.splitAndReplaceVariables(arguments, scope, true);
            List<String> commandLine = new LinkedList<String>();
            commandLine.add(command);
            commandLine.addAll(resolvedArguments);

            ProcessBuilder builder = new ProcessBuilder(commandLine);
            builder.redirectErrorStream(true);
            ByteHandler byteHandler;
            if(context.getOutputStream() == null)
            {
                byteHandler = new NullByteHandler();
            }
            else
            {
                byteHandler = new ForwardingByteHandler(context.getOutputStream());
            }

            asyncProcess = new AsyncProcess(builder.start(), byteHandler, false);
            if (timeoutApplied)
            {
                asyncProcess.waitForSuccess();
            }
            else
            {
                asyncProcess.waitForSuccessOrThrow(timeout, TimeUnit.SECONDS);
            }
        }
        finally
        {
            if (asyncProcess != null)
            {
                asyncProcess.destroy();
            }
        }
    }
}

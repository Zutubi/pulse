package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.tove.config.AbstractConfiguration;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.ByteHandler;
import com.zutubi.pulse.core.util.process.ForwardingByteHandler;
import com.zutubi.pulse.core.util.process.NullByteHandler;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Run executable tasks are used to execute an arbitrary command in a hook.
 */
@SymbolicName("zutubi.runExecutableTaskConfig")
@Form(fieldOrder = {"command", "arguments", "workingDir", "timeoutApplied", "timeout"})
public class RunExecutableTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
    @Required
    private String command;
    private String arguments;
    private String workingDir;
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

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
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
            Scope scope = context.getScope();
            List<String> resolvedArguments = VariableHelper.splitAndReplaceVariables(arguments, scope, VariableHelper.ResolutionStrategy.RESOLVE_NON_STRICT);
            List<String> commandLine = new LinkedList<String>();
            commandLine.add(command);
            commandLine.addAll(resolvedArguments);

            ProcessBuilder builder = new ProcessBuilder(commandLine);
            builder.redirectErrorStream(true);
            if(TextUtils.stringSet(workingDir))
            {
                builder.directory(new File(workingDir));
            }
            
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
                asyncProcess.waitForSuccessOrThrow(timeout, TimeUnit.SECONDS);
            }
            else
            {
                asyncProcess.waitForSuccess();
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

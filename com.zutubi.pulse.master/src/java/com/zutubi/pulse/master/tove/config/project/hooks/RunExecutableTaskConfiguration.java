package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.util.process.ProcessWrapper;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_AGENT_HANDLE;

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
    @ControllingCheckbox(checkedFields = {"timeout"})
    private boolean timeoutApplied;
    @Numeric(min = 0)
    private int timeout;

    @Transient
    private AgentManager agentManager;

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

    public void execute(ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode, boolean onAgent) throws Exception
    {
        List<String> resolvedArguments = context.splitAndResolveVariables(arguments);
        List<String> commandLine = new LinkedList<String>();
        commandLine.add(context.resolveVariables(command));
        commandLine.addAll(resolvedArguments);

        if (resultNode != null && onAgent)
        {
            executeOnAgent(context, commandLine);
        }
        else
        {
            String resolvedWorkingDir = StringUtils.stringSet(workingDir) ? context.resolveVariables(workingDir) : null;
            if (timeoutApplied)
            {
                ProcessWrapper.runCommand(commandLine, resolvedWorkingDir, context.getOutputStream(), timeout, TimeUnit.SECONDS);
            }
            else
            {
                ProcessWrapper.runCommand(commandLine, resolvedWorkingDir, context.getOutputStream());
            }
        }
    }

    private void executeOnAgent(ExecutionContext context, List<String> commandLine)
    {
        long agentHandle = context.getLong(PROPERTY_AGENT_HANDLE, 0);
        Agent agent = agentManager.getAgentByHandle(agentHandle);
        if (agent != null)
        {
            agent.getService().executeCommand(context, commandLine, workingDir, timeout);
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}

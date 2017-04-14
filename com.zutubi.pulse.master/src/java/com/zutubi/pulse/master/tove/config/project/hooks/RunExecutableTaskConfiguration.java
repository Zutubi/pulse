/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.api.PulseRuntimeException;
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
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_AGENT;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_AGENT_HANDLE;

/**
 * Run executable tasks are used to execute an arbitrary command in a hook.
 */
@SymbolicName("zutubi.runExecutableTaskConfig")
@Form(fieldOrder = {"command", "arguments", "workingDir", "timeoutApplied", "timeout"})
public class RunExecutableTaskConfiguration extends AbstractConfiguration implements BuildHookTaskConfiguration
{
    private static final Logger LOG = Logger.getLogger(RunExecutableTaskConfiguration.class);

    private static final String PROPERTY_DIRECTORY_WHITELIST = "pulse.hook.command.directory.whitelist";

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
        commandLine.add(verifyCommand(context.resolveVariables(command), onAgent));
        commandLine.addAll(resolvedArguments);

        String resolvedWorkingDir = StringUtils.stringSet(workingDir) ? context.resolveVariables(workingDir) : null;
        if (onAgent)
        {
            executeOnAgent(context, buildResult, resultNode, commandLine, resolvedWorkingDir);
        }
        else
        {
            ProcessWrapper.runCommand(commandLine, resolvedWorkingDir, (PulseExecutionContext) context, timeoutApplied ? timeout : ProcessWrapper.TIMEOUT_NONE, TimeUnit.SECONDS);
        }
    }

    private void executeOnAgent(ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode, List<String> commandLine, String resolvedWorkingDir)
    {
        Agent agent = null;
        if (resultNode == null)
        {
            // Post-build hook, we find an agent from the build result.
            for (RecipeResultNode candidateStage : buildResult.getStages())
            {
                agent = agentManager.getAgent(candidateStage.getAgentNameSafe());
                if (agent != null)
                {
                    break;
                }
            }

            if (agent == null)
            {
                throw new PulseRuntimeException("Could not execute hook on agent: build did not run on any current agents.");
            }
        }
        else
        {
            // Stage hook, look up the agent for this stage.
            long agentHandle = context.getLong(PROPERTY_AGENT_HANDLE, 0);
            agent = agentManager.getAgentByHandle(agentHandle);
            if (agent == null)
            {
                // If we are manually-triggered from a completed build, the agent handle is not known.
                // The best we have is the agent name, which should work most of the time (in the
                // worst case it could refer to a different machine now, but so be it!).
                String name = context.getString(PROPERTY_AGENT);
                if (StringUtils.stringSet(name))
                {
                    agent = agentManager.getAgent(name);
                }

                if (agent == null)
                {
                    throw new PulseRuntimeException("Could not execute hook on agent '" + name + "': no such agent exists");
                }
            }
        }

        agent.getService().executeCommand((PulseExecutionContext) context, commandLine, resolvedWorkingDir, timeout);
    }

    private String verifyCommand(String command, boolean onAgent) throws IOException
    {
        if (onAgent)
        {
            return command;
        }

        String whitelistString = System.getProperty(PROPERTY_DIRECTORY_WHITELIST);
        if (whitelistString == null)
        {
            return command;
        }
        else
        {
            // Note that apart from verifying the command is in the whitelist we must also ensure we return an
            // absolute path, otherwise when the command is run it is subject to a different PATH search which may hit
            // another exe.  We must also take care that relative paths are not used to escape the whitelist, e.g.
            //
            //   /some/whitelisted/dir/../../../bin/foo
            //
            // We do this by ensuring the file's parent is in the whitelist, even when constructing the file path based
            // on a whitelisted directory, and by canonicalising paths before comparing them.
            String[] whitelist = StringUtils.split(whitelistString, ';');
            for (String dirName : whitelist)
            {
                File directory = new File(dirName);
                directory = directory.getCanonicalFile();
                if (directory.isDirectory())
                {
                    File commandFile = new File(command);
                    if (!commandFile.isAbsolute())
                    {
                        commandFile = new File(directory, command);
                    }

                    commandFile = commandFile.getCanonicalFile();
                    if (commandFile.getParentFile().equals(directory) && commandFile.isFile())
                    {
                        LOG.info("Command '" + command + "' found in whitelisted directory '" + dirName + "'");
                        return commandFile.getAbsolutePath();
                    }
                }
                else
                {
                    LOG.warning("Ignoring whitelisted path '" + directory + "': not a directory");
                }
            }

            throw new IOException("Unable to locate command '" + command + "' in any whitelisted directory '" + whitelistString + "'");
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}

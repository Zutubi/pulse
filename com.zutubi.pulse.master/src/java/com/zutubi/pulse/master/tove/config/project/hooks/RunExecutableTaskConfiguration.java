package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.ByteHandler;
import com.zutubi.pulse.core.util.process.ForwardingByteHandler;
import com.zutubi.pulse.core.util.process.NullByteHandler;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
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
            List<String> resolvedArguments = context.splitAndResolveVariables(arguments);
            List<String> commandLine = new LinkedList<String>();
            commandLine.add(verifyCommand());
            commandLine.addAll(resolvedArguments);

            ProcessBuilder builder = new ProcessBuilder(commandLine);
            builder.redirectErrorStream(true);
            if(StringUtils.stringSet(workingDir))
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

    private String verifyCommand() throws IOException
    {
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
}

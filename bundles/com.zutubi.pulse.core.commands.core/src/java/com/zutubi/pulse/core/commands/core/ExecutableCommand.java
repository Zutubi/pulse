package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.RecipeUtils;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.util.process.ProcessControl;
import com.zutubi.tove.variables.api.Variable;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The executable command represents a os command invocation.
 *
 * It exposes two built in artifacts. The command's output and its execution
 * environment.
 */
public class ExecutableCommand extends OutputProducingCommandSupport
{
    /**
     * The name of the execution environment artifact.
     */
    static final String ENV_ARTIFACT_NAME = "environment";
    static final String ENV_FILENAME = "env.txt";

    public static final String OUTPUT_ARTIFACT_NAME = "command output";
    static final String OUTPUT_FILENAME = "output.txt";

    private Process child;
    private CancellableReader reader;
    private CancellableReader writer;
    private volatile boolean terminated = false;

    private List<String> suppressedEnvironment = RecipeUtils.getSuppressedEnvironment();

    public ExecutableCommand(ExecutableCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    public ExecutableCommandConfiguration getConfig()
    {
        return (ExecutableCommandConfiguration) super.getConfig();
    }

    public void execute(CommandContext commandContext, OutputStream outputStream)
    {
        ExecutionContext executionContext = commandContext.getExecutionContext();
        File workingDir = getWorkingDir(executionContext.getWorkingDir());
        ProcessBuilder builder = new ProcessBuilder(constructCommand(executionContext, workingDir));
        builder.directory(workingDir);
        updateChildEnvironment(executionContext, builder);

        builder.redirectErrorStream(true);

        // record the commands execution environment as an artifact.
        try
        {
            captureExecutionEnvironmentArtifact(commandContext, builder);
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to record the process execution environment. ", e);
        }

        File inFile = checkInputFile(workingDir);
        if (terminatedCheck(commandContext))
        {
            // Catches the case where we were asked to terminate before
            // creating the child process.
            return;
        }

        try
        {
            child = builder.start();
        }
        catch (IOException e)
        {
            // CIB-149: try and make friendlier error messages for common problems.
            String message = e.getMessage();
            if (message.contains("nosuchexe") || message.endsWith("error=2") || message.contains("error=2,"))
            {
                message = "No such executable '" + builder.command().get(0) + "'";
            }

            throw new BuildException("Unable to create process: " + message, e);
        }

        try
        {
            InputStream input = child.getInputStream();
            reader = new CancellableReader(input, outputStream);
            reader.start();

            if(inFile != null)
            {
                writer = new CancellableReader(new FileInputStream(inFile), child.getOutputStream());
                writer.start();
            }
            
            if (terminatedCheck(commandContext))
            {
                return;
            }

            final int result = child.waitFor();

            // Wait at least once: we want to allow the reader some time to
            // complete after we are terminated so that we can identify the
            // resource leak (see below).
            boolean readerComplete = reader.waitFor(10);
            while(!terminated && !readerComplete)
            {
                readerComplete = reader.waitFor(10);
            }

            if (readerComplete)
            {
                IOException ioe = reader.getIoError();
                if (ioe != null)
                {
                    throw new BuildException(ioe);
                }
            }
            else
            {
                // This is a case where we cannot clean up all child processes,
                // and so must cut the reader thread loose.  This happens on
                // Windows, and we have no better solution but to report the
                // resource leak.
                commandContext.error("Unable to cleanly terminate the child process tree.  It is likely that some orphaned processes remain.");
            }

            if (writer != null)
            {
                if (writer.waitFor(10))
                {
                    IOException ioe = writer.getIoError();
                    if (ioe != null)
                    {
                        throw new BuildException(ioe);
                    }
                }
            }
            
            String commandLine = extractCommandLine(builder);

            ResultState state = mapExitCode(result);
            switch(state)
            {
                case SUCCESS:
                    break;
                case FAILURE:
                    commandContext.failure("Command '" + commandLine + "' exited with code '" + result + "'");
                    break;
                default:
                    commandContext.error("Command '" + commandLine + "' exited with code '" + result + "'");
                    break;
            }

            commandContext.addCommandProperty("exit code", Integer.toString(result));
            commandContext.addCommandProperty("command line", commandLine);

            if (builder.directory() != null)
            {
                commandContext.addCommandProperty("working directory", builder.directory().getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        catch (InterruptedException e)
        {
            if(!terminatedCheck(commandContext))
            {
                throw new BuildException(e);
            }
        }
        finally
        {
            if (child != null)
            {
                child.destroy();
            }
        }
    }

    private ResultState mapExitCode(int code)
    {
        for(StatusMappingConfiguration mapping: getConfig().getStatusMappings())
        {
            if(mapping.getCode() == code)
            {
                return mapping.getStatus();
            }
        }

        if(code == 0)
        {
            return ResultState.SUCCESS;
        }
        else
        {
            return ResultState.FAILURE;
        }
    }

    private File checkInputFile(File workingDir)
    {
        String inputFile = getConfig().getInputFile();
        if(StringUtils.stringSet(inputFile))
        {
            File input = new File(workingDir, inputFile);
            if(!input.exists())
            {
                throw new BuildException("Input file '" + input.getAbsolutePath() + "' does not exist");
            }

            if(!input.canRead())
            {
                throw new BuildException("Input file '" + input.getAbsolutePath() + "' is not readable");
            }

            return input;
        }

        return null;
    }

    private boolean terminatedCheck(CommandContext commandContext)
    {
        if(terminated)
        {
            commandContext.error("Command terminated");
            return true;
        }

        return false;
    }

    private void captureExecutionEnvironmentArtifact(CommandContext commandContext, ProcessBuilder builder) throws IOException
    {
        File envFileDir = commandContext.registerArtifact(ENV_ARTIFACT_NAME, null);
        File file = new File(envFileDir, ENV_FILENAME);

        final String separator = Constants.LINE_SEPARATOR;

        // buffered contents to be written to the file later.
        StringBuffer buffer = new StringBuffer();

        buffer.append("Command Line:").append(separator);
        buffer.append("-------------").append(separator);
        buffer.append(extractCommandLine(builder)).append(separator);

        buffer.append(separator);
        buffer.append("Process Environment:").append(separator);
        buffer.append("--------------------").append(separator);

        // use a tree map to provide ordering to the keys.
        Map<String, String> env = new TreeMap<String, String>(builder.environment());
        for (String key : env.keySet())
        {
            String value = env.get(key);
            appendProperty(key, value, buffer, separator);
        }

        buffer.append(separator);
        buffer.append("Resources: (via scope)").append(separator);
        buffer.append("----------------------").append(separator);

        PulseScope scope = ((PulseExecutionContext) commandContext.getExecutionContext()).getScope();
        if (scope.getEnvironment().size() > 0)
        {
            for (Map.Entry<String, String> setting : scope.getEnvironment().entrySet())
            {
                appendProperty(setting.getKey(), setting.getValue(), buffer, separator);
            }
        }
        else
        {
            buffer.append("No environment variables defined via the command scope.").append(separator);
        }

        buffer.append(separator);
        buffer.append("Resources: (via environment tag)").append(separator);
        buffer.append("--------------------------------").append(separator);
        List<EnvironmentConfiguration> configuredEnv = getConfig().getEnvironments();
        if (configuredEnv.size() > 0)
        {
            for (EnvironmentConfiguration setting : configuredEnv)
            {
                appendProperty(setting.getName(), setting.getValue(), buffer, separator);
            }
        }
        else
        {
            buffer.append("No environment variables defined via the command env tags.").append(separator);
        }

        // write the buffer to the file.
        Writer writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.append(buffer);
            writer.flush();
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    private void appendProperty(String key, String value, StringBuffer buffer, String separator)
    {
        if (suppressedEnvironment.contains(key.toUpperCase()))
        {
            value = RecipeUtils.SUPPRESSED_VALUE;
        }
        buffer.append(key).append("=").append(value).append(separator);
    }

    private List<String> constructCommand(ExecutionContext context, File workingDir)
    {
        String binary = determineExe(context);

        File exeFile = new File(binary);
        if (!exeFile.isAbsolute())
        {
            // CIB-902: search relative to the working directory before going
            // to the path.
            File relativeToWork = new File(workingDir, binary);
            if(relativeToWork.exists())
            {
                binary = relativeToWork.getAbsolutePath();
            }
            else
            {
                exeFile = SystemUtils.findInPath(binary, ((PulseExecutionContext) context).getScope().getPathDirectories());
                if (exeFile != null)
                {
                    binary = exeFile.getAbsolutePath();
                }
            }
        }

        List<String> command = new LinkedList<String>();
        command.add(binary);
        command.addAll(getConfig().getCombinedArguments());
        return command;
    }

    private String determineExe(ExecutionContext context)
    {
        ExecutableCommandConfiguration configuration = getConfig();
        String exe = configuration.getExe();
        if (!StringUtils.stringSet(exe))
        {
            if (StringUtils.stringSet(configuration.getExeProperty()))
            {
                exe = context.getString(configuration.getExeProperty());
            }

            if (!StringUtils.stringSet(exe))
            {
                exe = configuration.getDefaultExe();
            }
        }

        return exe;
    }

    /**
     * The working directory for this command is calculated as follows:
     * 1) defaults to the recipe paths base directory.
     * 2) if working dir is specified, then
     *   a) if it is absolute, this is the working directory.
     *   b) if it is relative, then it is taken as the directory relative to the base directory.
     *
     * @param baseDir the base directory for the recipe
     * @return working directory for the command.
     * @throws BuildException if the user-configured working directory does not
     *         exist
     */
    protected File getWorkingDir(File baseDir)
    {
        File workingDir;
        if (StringUtils.stringSet(getConfig().getWorkingDir()))
        {
            workingDir = new File(getConfig().getWorkingDir());
            if (!workingDir.isAbsolute())
            {
                workingDir = new File(baseDir, workingDir.getPath());
            }

            if (!workingDir.exists())
            {
                throw new BuildException("Working directory '" + getConfig().getWorkingDir() + "' does not exist");
            }

            if (!workingDir.isDirectory())
            {
                throw new BuildException("Working directory '" + getConfig().getWorkingDir() + "' exists, but is not a directory");
            }
        }
        else
        {
            workingDir = baseDir;
        }

        return workingDir;
    }

    private void updateChildEnvironment(ExecutionContext context, ProcessBuilder builder)
    {
        Map<String, String> childEnvironment = builder.environment();
        // Implicit PULSE_* varialbes come first: anything explicit
        // should override them.
        PulseScope scope = ((PulseExecutionContext) context).getScope();
        for(Variable variable : scope.getVariables(String.class))
        {
            if(acceptableName(variable.getName()))
            {
                childEnvironment.put(convertName(variable.getName()), (String) variable.getValue());
            }
        }

        // Now things defined on the scope.
        scope.applyEnvironment(childEnvironment);

        // Finally things defined on the command
        for (EnvironmentConfiguration setting : getConfig().getEnvironments())
        {
            childEnvironment.put(setting.getName(), setting.getValue());
        }
    }

    /**
     * Is the specified name an acceptable name for adding to the child processes environment.
     * If it is already in the environment (env. prefix), then we return false.
     *
     * @param name variable name to check
     *
     * @return return false if the name contains the 'env.' prefix, or contains an unsupported
     * character
     */
    protected boolean acceptableName(String name)
    {
        if(name.startsWith("env."))
        {
            return false;
        }

        if(suppressedEnvironment.contains(name.toUpperCase()))
        {
            return false;
        }
        
        if (SystemUtils.IS_WINDOWS)
        {
            return name.matches("[-a-zA-Z._0-9<>|&^% ]+");
        }

        return name.matches("[-a-zA-Z._0-9]+");
    }

    protected String convertName(String name)
    {
        name = name.toUpperCase();
        name = name.replaceAll("\\.", "_");

        return "PULSE_" + name;
    }

    private String extractCommandLine(ProcessBuilder builder)
    {
        StringBuffer result = new StringBuffer();
        boolean first = true;

        for (String part : builder.command())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                result.append(' ');
            }

            boolean containsSpaces = part.indexOf(' ') != -1;

            if (containsSpaces)
            {
                result.append('"');
            }

            result.append(part);

            if (containsSpaces)
            {
                result.append('"');
            }
        }

        return result.toString();
    }

    public void terminate()
    {
        terminated = true;
        if (child != null)
        {
            ProcessControl.destroyProcess(child);
            child = null;
        }

        if(reader != null)
        {
            reader.cancel();
        }
    }

    class CancellableReader
    {
        private static final long WAIT_LIMIT = 60 * Constants.SECOND;

        private Thread readerThread;
        private InputStream in;
        private OutputStream out;
        private IOException ioError = null;
        private boolean interrupted = false;
        /**
         * Set to the current time the first time that {@link #waitFor(long)}
         * is called, as a base point to time out waits.  Updated when new
         * output is read after this point, so if we actually do have a lot to
         * drain we don't time out.
         */
        private long waitTimestamp = -1;

        public CancellableReader(InputStream in, OutputStream out)
        {
            this.in = in;
            this.out = out;
        }

        public synchronized void start()
        {
            readerThread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        byte[] buffer = new byte[1024];
                        int n;

                        while (!isCancelled() && (n = in.read(buffer)) > 0)
                        {
                            synchronized (CancellableReader.this)
                            {
                                if (n > 0 && waitTimestamp > 0)
                                {
                                    waitTimestamp = System.currentTimeMillis();
                                }
                            }

                            out.write(buffer, 0, n);
                        }
                    }
                    catch (IOException e)
                    {
                        ioError = e;
                    }
                    finally
                    {
                        close();
                    }
                }
            });
            readerThread.start();
        }

        private void close()
        {
            IOUtils.close(in);
            IOUtils.close(out);
        }

        public synchronized void cancel()
        {
            interrupted = true;
        }

        private synchronized boolean isCancelled()
        {
            return interrupted || Thread.interrupted();
        }

        public boolean waitFor(long seconds)
        {
            synchronized (this)
            {
                long currentTime = System.currentTimeMillis();
                if (waitTimestamp < 0)
                {
                    waitTimestamp = currentTime;
                }
                else if (currentTime - waitTimestamp > WAIT_LIMIT)
                {
                    // The process has exited, and we haven't seen any new
                    // output for the timeout, so assume there is no more to
                    // see.
                    readerThread.interrupt();
                    return true;
                }
            }

            try
            {
                readerThread.join(Constants.SECOND * seconds);
            }
            catch (InterruptedException e)
            {
                // Empty
            }

            return !readerThread.isAlive();
        }

        public IOException getIoError()
        {
            return ioError;
        }
    }
}

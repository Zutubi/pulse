package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.ForkOutputStream;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.SystemUtils;

import java.io.*;
import java.util.*;

/**
 * The executable command represents a os command invocation.
 *
 * It exposes two built in artifacts. The commands output and the commands execution
 * environment.
 *
 */
public class ExecutableCommand extends CommandSupport implements ScopeAware
{
    /**
     * The name of the execution environment artifact.
     */
    public static final String ENV_ARTIFACT_NAME = "environment";
    private static final String ENV_PATH = "PATH";

    private String exe;
    private List<Arg> args = new LinkedList<Arg>();
    private File workingDir;
    private List<Environment> env = new LinkedList<Environment>();

    private Scope scope;

    private Process child;
    private CancellableReader reader;
    private volatile boolean terminated = false;

    private FileArtifact outputArtifact;
    private FileArtifact envArtifact;
    private static final String ENV_FILENAME = "env.txt";

    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();

    /**
     * Required no arg constructor.
     */
    public ExecutableCommand()
    {

    }

    public void execute(CommandContext context, CommandResult cmdResult)
    {
        ProcessBuilder builder = new ProcessBuilder(constructCommand());
        builder.directory(getWorkingDir(context.getPaths()));
        updateChildEnvironment(builder, context);

        builder.redirectErrorStream(true);

        // record the commands execution environment as an artifact.
        try
        {
            captureExecutionEnvironmentArtifact(builder, context.getOutputDir());
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to record the process execution environment. ", e);
        }

        File outputFileDir = new File(context.getOutputDir(), OUTPUT_ARTIFACT_NAME);
        if (!outputFileDir.mkdir())
        {
            throw new BuildException("Unable to create directory for the output artifact '" + outputFileDir.getAbsolutePath() + "'");
        }

        if (terminatedCheck(cmdResult))
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
            if (message.contains("nosuchexe") || message.endsWith("error=2"))
            {
                message = "No such executable '" + exe + "'";
            }
            else if (message.endsWith("error=267"))
            {
                message = "Working directory '" + workingDir.getPath() + "' does not exist";
            }

            throw new BuildException("Unable to create process: " + message, e);
        }

        // capture the command output.
        File outputFile = new File(outputFileDir, OUTPUT_FILENAME);

        try
        {
            // initialise the output artifacts.
            initialiseOutputArtifact();

            FileOutputStream outputFileStream = new FileOutputStream(outputFile);
            OutputStream output;

            if (context.getOutputStream() != null)
            {
                output = new ForkOutputStream(outputFileStream, context.getOutputStream());
            }
            else
            {
                output = outputFileStream;
            }

            InputStream input = child.getInputStream();
            reader = new CancellableReader(input, output);
            reader.start();

            if (terminatedCheck(cmdResult))
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
                reader.waitFor(10);
            }

            if(readerComplete)
            {
                IOException ioe = reader.getIoError();
                if(ioe != null)
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
                cmdResult.error("Unable to cleanly terminate the child process tree.  It is likely that some orphaned processes remain.");
            }

            String commandLine = extractCommandLine(builder);

            if (result == 0)
            {
                cmdResult.success();
            }
            else
            {
                cmdResult.failure("Command '" + commandLine + "' exited with code '" + result + "'");
            }

            cmdResult.getProperties().put("exit code", Integer.toString(result));
            cmdResult.getProperties().put("command line", commandLine);

            if (builder.directory() != null)
            {
                cmdResult.getProperties().put("working directory", builder.directory().getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        catch (InterruptedException e)
        {
            if(!terminatedCheck(cmdResult))
            {
                throw new BuildException(e);
            }
        }
    }

    private void initialiseOutputArtifact()
    {
        outputArtifact = new FileArtifact();
        outputArtifact.setName(OUTPUT_ARTIFACT_NAME);
        outputArtifact.setFailIfNotPresent(false);
        outputArtifact.setIgnoreStale(false);
        outputArtifact.setOutputArtifact(true);
        outputArtifact.setFile(OUTPUT_FILENAME);
        outputArtifact.setType("text/plain");
        outputArtifact.setProcesses(processes);
    }

    public List<Artifact> getArtifacts()
    {
        List<Artifact> artifacts = new LinkedList<Artifact>();
        if (envArtifact != null)
        {
            artifacts.add(envArtifact);
        }
        if (outputArtifact != null)
        {
            artifacts.add(outputArtifact);
        }
        return artifacts;
    }

    private boolean terminatedCheck(CommandResult commandResult)
    {
        if(terminated)
        {
            commandResult.error("Command terminated");
            return true;
        }

        return false;
    }

    /**
     * This method records the execution environment and adds it as an artifact to the command result.
     *
     * @param builder is the configured builder used to execute the command.
     * @param outputDir is the artifact output directory.
     *
     * @throws IOException if there are problems recording the execution environment.
     */
    private void captureExecutionEnvironmentArtifact(ProcessBuilder builder, File outputDir) throws IOException
    {
        initialiseEnvironmentArtifact();

        File envFileDir = new File(outputDir, ENV_ARTIFACT_NAME);
        if (!envFileDir.mkdir())
        {
            throw new BuildException("Unable to create directory for the environment artifact '" + envFileDir.getAbsolutePath() + "'");
        }

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
            buffer.append(key).append("=").append(env.get(key)).append(separator);
        }

        buffer.append(separator);
        buffer.append("Resources: (via scope)").append(separator);
        buffer.append("----------------------").append(separator);
        if (scope != null && scope.getEnvironment().size() > 0)
        {
            for (Map.Entry<String, String> setting : scope.getEnvironment().entrySet())
            {
                buffer.append(setting.getKey()).append("=").append(setting.getValue()).append(separator);
            }
        }
        else
        {
            buffer.append("No environment variables defined via the command scope.").append(separator);
        }

        buffer.append(separator);
        buffer.append("Resources: (via environment tag)").append(separator);
        buffer.append("--------------------------------").append(separator);
        if (this.env.size() > 0)
        {
            for (Environment setting : this.env)
            {
                buffer.append(setting.getName()).append("=").append(setting.getValue()).append(separator);
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
            writer.append(buffer.toString());
            writer.flush();
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    private void initialiseEnvironmentArtifact()
    {
        // Configure the environment artifact.
        envArtifact = new FileArtifact();
        envArtifact.setName(ENV_ARTIFACT_NAME);
        envArtifact.setFailIfNotPresent(false);
        envArtifact.setIgnoreStale(false);
        envArtifact.setOutputArtifact(true);
        envArtifact.setFile(ENV_FILENAME);
        envArtifact.setType("text/plain");
    }

    private List<String> constructCommand()
    {
        String binary = exe;

        File exeFile = new File(exe);
        if (!exeFile.isAbsolute())
        {
            exeFile = SystemUtils.findInPath(exe, scope == null ? null : scope.getPathDirectories());
            if (exeFile != null)
            {
                binary = exeFile.getAbsolutePath();
            }
        }

        List<String> command = new LinkedList<String>();
        command.add(binary);

        if (args != null)
        {
            for (Arg arg : args)
            {
                command.add(arg.getText());
            }
        }
        return command;
    }

    /**
     * The working directory for this command is calculated as follows:
     * 1) defaults to the recipe paths base directory.
     * 2) if working dir is specified, then
     *   a) if it is absolute, this is the working directory.
     *   b) if it is relative, then it is taken as the directory relative to the base directory.
     *
     * @param paths
     *
     * @return working directory for the command.
     */
    protected File getWorkingDir(RecipePaths paths)
    {
        if (workingDir == null)
        {
            return paths.getBaseDir();
        }
        else
        {
            if (workingDir.isAbsolute())
            {
                return workingDir;
            }
            else
            {
                return new File(paths.getBaseDir(), workingDir.getPath());
            }
        }
    }

    private void updateChildEnvironment(ProcessBuilder builder, CommandContext context)
    {
        Map<String, String> childEnvironment = builder.environment();

        if (scope != null)
        {
            for (Map.Entry<String, String> setting : scope.getEnvironment().entrySet())
            {
                childEnvironment.put(setting.getKey(), setting.getValue());
            }

            /**
             * Here, we are using a custom get property method that runs a case insensitive lookup of the ENV_PATH
             * property. This is required because on Windows, the PATH variable is actually in the map as Path, and
             * so is not matched. 
             */
            String pathKey = ENV_PATH;
            String pathValue = scope.getPathPrefix();

            String translatedKey = locateCaseInsensitiveMatch(ENV_PATH, childEnvironment);
            if (translatedKey != null)
            {
                String path = childEnvironment.get(translatedKey);
                pathValue = pathValue + path;
            }

            childEnvironment.put(pathKey, pathValue);
        }

        for (Environment setting : env)
        {
            childEnvironment.put(setting.getName(), setting.getValue());
        }

        Scope globalScope = context.getGlobalScope();
        if(globalScope != null)
        {
            for(Reference reference: globalScope.getReferences())
            {
                if(acceptableName(reference.getName()) && reference.getValue() instanceof String)
                {
                    String value = (String) reference.getValue();

                    childEnvironment.put(convertName(reference.getName()), value);
                }
            }
        }
    }

    /**
     * Is the specified name an acceptable name for adding to the child processes environment.
     * If it is already in the environment (env. prefix), then we return false.
     *
     * @param name
     *
     * @return return false if the name contains the 'env.' prefix, or contains an unsupported
     * character
     */
    private boolean acceptableName(String name)
    {
        if(name.startsWith("env."))
        {
            return false;
        }

        return name.matches("[-a-zA-Z._]+");
    }

    private String convertName(String name)
    {
        name = name.toUpperCase();
        name = name.replaceAll("\\.", "_");
        return "PULSE_" + name;
    }

    /**
     * Take the specified key, and find a case insensitive match in the maps key set. Return this match,
     * or null if no match is found.
     *
     * This is important when we are looking for environment variables (such as PATH) that may be
     * specified in lowercase, as so happens when we use the environment variables from the process
     * builder.
     */
    private String locateCaseInsensitiveMatch(String propertyName, Map<String, String> map)
    {
        // case insensitive lookup.
        propertyName = propertyName.toLowerCase();
        for (String key : map.keySet())
        {
            if (key.toLowerCase().equals(propertyName))
            {
                return key;
            }
        }
        return null;
    }

    protected Scope getScope()
    {
        return scope;
    }

    public List<String> getArtifactNames()
    {
        return Arrays.asList(OUTPUT_ARTIFACT_NAME);
    }

    public String getExe()
    {
        return exe;
    }

    public void setExe(String exe)
    {
        this.exe = exe;
    }

    /**
     * Allow the setting of arguments in the form of a space separated list.
     *
     * @param args is a space separated list of arguments.
     */
    public void setArgs(String args)
    {
        for (String arg : args.split(" "))
        {
            Arg a = createArg();
            a.setText(arg);
        }
    }

    public void setWorkingDir(File d)
    {
        this.workingDir = d;
    }

    public Arg createArg()
    {
        Arg arg = new Arg();
        args.add(arg);
        return arg;
    }

    /**
     * Convenience method for programatically adding arguments to this command.
     *
     * @param arguments to be added.
     */
    protected void addArguments(String ...arguments)
    {
        for (String arg : arguments)
        {
            Arg argument = createArg();
            argument.setText(arg);
        }
    }

    public Environment createEnvironment()
    {
        Environment setting = new Environment();
        env.add(setting);
        return setting;
    }

    public ProcessArtifact createProcess()
    {
        ProcessArtifact p = new ProcessArtifact();
        processes.add(p);
        return p;
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
            child.destroy();
        }

        if(reader != null)
        {
            reader.cancel();
        }
    }

    List<Arg> getArgs()
    {
        return args;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    /**
     *
     */
    public class Arg
    {
        private String text;

        public Arg()
        {
            text = "";
        }

        public Arg(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text += text;
        }
    }

    /**
     * The nested environment tag definition.  Instances of this class are added
     * to the execution environment of ths command.
     */
    public class Environment
    {
        /**
         * The name of the environment property.
         */
        private String name;

        /**
         * The value of the environment property.
         */
        private String value;

        public Environment()
        {
            value = "";
        }

        /**
         * Getter for the name property.
         *
         * @return the environment variable name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Setter for the name property.
         *
         * @param name the environment variable name.
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * Getter for the value property.
         *
         * @return the environment variable value.
         */
        public String getValue()
        {
            return value;
        }

        /**
         * Setter for the value property.
         *
         * @param value the environment variable value.
         */
        public void setValue(String value)
        {
            this.value = value;
        }

        /**
         * This setter supports defining the value of this environment variable as the
         * body text of the tag. For example:
         *
         * <env name="foo">bar</env>
         *
         * @param text the environment variable value.
         */
        public void setText(String text)
        {
            this.value += text;
        }

        public String getText()
        {
            return this.value;
        }
    }

    class CancellableReader
    {
        private Thread readerThread;
        private InputStream in;
        private OutputStream out;
        private IOException ioError = null;
        private boolean interrupted = false;

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

                        while (!interrupted && !Thread.interrupted() && (n = in.read(buffer)) > 0)
                        {
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

        public boolean waitFor(long seconds)
        {
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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.*;

import java.io.*;
import java.util.*;

/**
 *
 *
 */
public class ExecutableCommand implements Command, ScopeAware
{
    public static final String OUTPUT_NAME = "command output";
    public static final String ENV_NAME = "environment";
    private static final String ENV_PATH = "PATH";

    private String name;
    private String exe;
    private List<Arg> args = new LinkedList<Arg>();
    private File workingDir;
    private List<Environment> env = new LinkedList<Environment>();
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();
    private Scope scope;

    private Process child;
    private CancellableReader reader;
    private volatile boolean terminated = false;


    public void execute(CommandContext context, CommandResult cmdResult)
    {
        ProcessBuilder builder = new ProcessBuilder(constructCommand());
        updateWorkingDir(builder, context.getPaths());
        updateChildEnvironment(builder, context);

        builder.redirectErrorStream(true);

        File envFileDir = new File(context.getOutputDir(), ENV_NAME);
        if (!envFileDir.mkdir())
        {
            throw new BuildException("Unable to create directory for the environment artifact '" + envFileDir.getAbsolutePath() + "'");
        }

        // record the commands execution environment as an artifact.
        try
        {
            recordExecutionEnvironment(builder, cmdResult, envFileDir);
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to record the process execution environment. ", e);
        }

        File outputFileDir = new File(context.getOutputDir(), OUTPUT_NAME);
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

        File outputFile = new File(outputFileDir, "output.txt");

        try
        {
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

            String commandLine = constructCommandLine(builder);

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
        finally
        {
            // In finally so that any output gathered will be captured as an
            // artifact, even if the command failed.
            if(outputFile.exists())
            {
                ProcessSupport.postProcess(processes, outputFileDir, outputFile, cmdResult, context);
            }
        }
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

    private void recordExecutionEnvironment(ProcessBuilder builder, CommandResult cmdResult, File outputDir) throws IOException
    {
        File file = new File(outputDir, "env.txt");

        final String separator = Constants.LINE_SEPARATOR;

        // buffered contents to be written to the file later.
        StringBuffer buffer = new StringBuffer();

        buffer.append("Command Line:").append(separator);
        buffer.append("-------------").append(separator);
        buffer.append(constructCommandLine(builder)).append(separator);

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

        // capture the artifact.
        String path = FileSystemUtils.composeFilename(outputDir.getName(), file.getName());

        StoredArtifact envArtifact = new StoredArtifact(ENV_NAME, new StoredFileArtifact(path, "text/plain"));
        cmdResult.addArtifact(envArtifact);
    }

    protected StoredFileArtifact getOutputFileArtifact(CommandResult result)
    {
        StoredArtifact outputArtifact = result.getArtifact(OUTPUT_NAME);
        for (StoredFileArtifact file : outputArtifact.getChildren())
        {
            if (file.getPath().equals(OUTPUT_NAME + "/output.txt"))
            {
                return file;
            }
        }
        return null;
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

    private void updateWorkingDir(ProcessBuilder builder, RecipePaths paths)
    {
        if (workingDir == null)
        {
            builder.directory(paths.getBaseDir());
        }
        else
        {
            if (workingDir.isAbsolute())
            {
                builder.directory(workingDir);
            }
            else
            {
                builder.directory(new File(paths.getBaseDir(), workingDir.getPath()));
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

            String translatedKey = translateKey(ENV_PATH, childEnvironment);
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

        if (context.getBuildNumber() != -1)
        {
            childEnvironment.put("PULSE_BUILD_NUMBER", Long.toString(context.getBuildNumber()));
        }
    }

    /**
     * Take the specified key, and find a case insensitive match in the maps key set. Return this match,
     * or null if no match is found.
     */
    private String translateKey(String propertyName, Map<String, String> map)
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
        return Arrays.asList(OUTPUT_NAME);
    }

    public String getExe()
    {
        return exe;
    }

    public void setExe(String exe)
    {
        this.exe = exe;
    }

    public void setArgs(String args)
    {
        for (String arg : args.split(" "))
        {
            Arg a = createArg();
            a.addText(arg);
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

    protected void addArguments(String ...arguments)
    {
        for (String arg : arguments)
        {
            Arg argument = new Arg(arg);
            args.add(argument);
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

    private String constructCommandLine(ProcessBuilder builder)
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

        public void addText(String text)
        {
            this.text += text;
        }

        public String getText()
        {
            return text;
        }
    }

    /**
     */
    public class Environment
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

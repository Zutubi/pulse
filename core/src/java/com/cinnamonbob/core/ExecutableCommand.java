package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class ExecutableCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(ExecutableCommand.class);

    public static final String OUTPUT_NAME = "command output";

    private String name;
    private String exe;
    private List<Arg> args = new LinkedList<Arg>();
    private File workingDir;
    private List<Environment> env = new LinkedList<Environment>();
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();

    private Process child;
    private volatile boolean terminated = false;

    public void execute(File baseDir, File outputDir, CommandResult cmdResult)
    {
        List<String> command = new LinkedList<String>();
        command.add(exe);

        if (args != null)
        {
            for (Arg arg : args)
            {
                command.add(arg.getText());
            }
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDir == null)
        {
            builder.directory(baseDir);
        }
        else
        {
            builder.directory(workingDir);
        }

        for (Environment setting : env)
        {
            builder.environment().put(setting.getName(), setting.getValue());
        }

        builder.redirectErrorStream(true);

        File outputFileDir = new File(outputDir, "command output");
        if (!outputFileDir.mkdir())
        {
            throw new BuildException("Unable to create directory for output artifact '" + outputFileDir.getAbsolutePath() + "'");
        }

        try
        {
            child = builder.start();
        }
        catch (IOException e)
        {
            // CIB-149: try and make friendlier error messages for common
            // problems.
            String message = e.getMessage();
            if (message.contains("nosuchexe") || message.contains("error=2"))
            {
                message = "No such executable '" + exe + "'";
            }

            throw new BuildException("Unable to create process: " + message, e);
        }

        if (terminated)
        {
            // Catches the case where we were asked to terminate before
            // creating the child process.
            cmdResult.error("Command terminated");
            return;
        }

        try
        {
            File outputFile = new File(outputFileDir, "output.txt");
            FileOutputStream output = null;
            try
            {
                output = new FileOutputStream(outputFile);
                InputStream input = child.getInputStream();

                IOUtils.joinStreams(input, output);
            }
            finally
            {
                IOUtils.close(output);
            }

            final int result = child.waitFor();
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

            String path = FileSystemUtils.composeFilename(outputFileDir.getName(), outputFile.getName());
            StoredFileArtifact fileArtifact = new StoredFileArtifact(path, "text/plain");
            StoredArtifact artifact = new StoredArtifact("command output", fileArtifact);
            for (ProcessArtifact p : processes)
            {
                p.getProcessor().process(outputDir, fileArtifact, cmdResult);
            }
            cmdResult.addArtifact(artifact);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        catch (InterruptedException e)
        {
            throw new BuildException(e);
        }
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
            this.args.add(new Arg(arg));
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
    }

    List<Arg> getArgs()
    {
        return args;
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
}

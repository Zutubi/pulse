package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.util.ForkOutputStream;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.SystemUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public class ExecutableCommand implements Command, ScopeAware
{
    private static final Logger LOG = Logger.getLogger(ExecutableCommand.class);

    public static final String OUTPUT_NAME = "command output";

    private String name;
    private String exe;
    private List<Arg> args = new LinkedList<Arg>();
    private File workingDir;
    private List<Environment> env = new LinkedList<Environment>();
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();
    private Scope scope;

    private Process child;
    private volatile boolean terminated = false;

    public void execute(long recipeId, CommandContext context, CommandResult cmdResult)
    {
        ProcessBuilder builder = new ProcessBuilder(constructCommand());
        updateWorkingDir(builder, context.getPaths());
        updateChildEnvironment(builder);

        builder.redirectErrorStream(true);

        File outputFileDir = new File(context.getOutputDir(), "command output");
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
            FileOutputStream outputFileStream = null;
            OutputStream output = null;

            try
            {
                outputFileStream = new FileOutputStream(outputFile);
                if(context.getOutputStream() != null)
                {
                    output = new ForkOutputStream(outputFileStream, context.getOutputStream());
                }
                else
                {
                    output = outputFileStream;
                }

                InputStream input = child.getInputStream();

                IOUtils.joinStreams(input, output);
            }
            finally
            {
                IOUtils.close(outputFileStream);
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

            ProcessSupport.postProcess(processes, outputFileDir, outputFile, context.getOutputDir(), cmdResult);
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

    private List<String> constructCommand()
    {
        String binary = exe;

        File exeFile = new File(exe);
        if(!exeFile.isAbsolute())
        {
            exeFile = SystemUtils.findInPath(exe, scope == null ? null : scope.getPathDirectories().values());
            if(exeFile != null)
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
            if(workingDir.isAbsolute())
            {
                builder.directory(workingDir);
            }
            else
            {
                builder.directory(new File(paths.getBaseDir(), workingDir.getPath()));
            }
        }
    }

    private void updateChildEnvironment(ProcessBuilder builder)
    {
        Map<String, String> childEnvironment = builder.environment();

        if (scope != null)
        {
            for(Map.Entry<String, String> setting: scope.getEnvironment().entrySet())
            {
                childEnvironment.put(setting.getKey(), setting.getValue());
            }
        }

        for (Environment setting : env)
        {
            childEnvironment.put(setting.getName(), setting.getValue());
        }
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
}

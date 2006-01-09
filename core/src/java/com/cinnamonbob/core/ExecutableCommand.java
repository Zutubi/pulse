package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.util.IOUtils;

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
    private String exe;
    private List<Arg> args = new LinkedList<Arg>();
    private File workingDir;

    private String name;

    private List<Environment> env = new LinkedList<Environment>();

    public void execute(File outputDir, CommandResult cmdResult)
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
        if (workingDir != null)
        {
            builder.directory(workingDir);
        }

        for (Environment setting : env)
        {
            builder.environment().put(setting.getName(), setting.getValue());
        }

        builder.redirectErrorStream(true);

        try
        {
            Process child = builder.start();
            File outputFile = new File(outputDir, "output.txt");
            FileOutputStream output = new FileOutputStream(outputFile);
            InputStream input = child.getInputStream();

            IOUtils.joinStreams(input, output);
            final int result = child.waitFor();

            output.close();

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

            // TODO awkward to add this stored artifact to the model...
            FileArtifact outputArtifact = new FileArtifact("output", outputFile);
            outputArtifact.setTitle("command output");
            outputArtifact.setType("text/plain");
            cmdResult.addArtifact(new StoredArtifact(outputArtifact, outputFile.getName()));
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
        return Arrays.asList("output");
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

    public Environment createEnvironment()
    {
        Environment setting = new Environment();
        env.add(setting);
        return setting;
    }

    private String constructCommandLine(ProcessBuilder builder)
    {
        StringBuffer result = new StringBuffer();

        for (String part : builder.command())
        {
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
            result.append(' ');
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
     * 
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

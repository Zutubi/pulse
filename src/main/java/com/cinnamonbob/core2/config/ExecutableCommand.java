package com.cinnamonbob.core2.config;

import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.util.IOHelper;

import java.util.List;
import java.util.LinkedList;
import java.io.*;

/**
 * 
 *
 */
public class ExecutableCommand implements Command
{
    private String exe;
    private String args;
    private File workingDir;
    
    private List<Environment> env = new LinkedList<Environment>();
    
    public CommandResult execute(File outputDir) throws CommandException
    {
        List<String> command = new LinkedList<String>();
        command.add(exe);
        
        if (args != null)
        {
            for (String arg : args.split(" "))
            {
                command.add(arg);
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
            
            IOHelper.joinStreams(input, output);            
            final int result = child.waitFor();
            
            output.close();
            
            CommandResult cmdResult = new CommandResult();
            cmdResult.setSucceeded(result == 0);
            cmdResult.getProperties().put("exit code", Integer.toString(result));
            cmdResult.getProperties().put("command line", constructCommandLine(builder));
            // TODO not always there
            cmdResult.getProperties().put("working directory", builder.directory().getAbsolutePath());
            
            // TODO awkward to add this stored artifact to the result...
            FileArtifact outputArtifact = new FileArtifact("output", outputFile);
            outputArtifact.setTitle("Command Output");
            outputArtifact.setType("text/plain");
            cmdResult.addArtifact(new StoredArtifact(outputArtifact, outputFile.getAbsolutePath()));
            
            return cmdResult;
        }
        catch (IOException e)
        {
            throw new CommandException(e);
        } 
        catch (InterruptedException e)
        {
            throw new CommandException(e);
        }        
    }

    public void setExe(String exe)
    {
        this.exe = exe;
    }
    
    public void setArgs(String args)
    {
        this.args = args;
    }

    public void setWorkingDir(File d)
    {
        this.workingDir = d;
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
        
        for(String part: builder.command())
        {
            boolean containsSpaces = part.indexOf(' ') != -1;
            
            if(containsSpaces)
            {
                result.append('"');
            }

            result.append(part);
            
            if(containsSpaces)
            {
                result.append('"');
            }
            result.append(' ');
        }
        
        return result.toString();
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

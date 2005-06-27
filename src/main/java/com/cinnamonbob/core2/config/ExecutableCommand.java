package com.cinnamonbob.core2.config;

import com.cinnamonbob.util.IOHelper;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;

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
    
    public CommandResult execute() throws CommandException
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
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            InputStream input = child.getInputStream();
            
            IOHelper.joinStreams(input, output);            
            final int result = child.waitFor();
            
            // stdout is the output artifact.
            String stdout = output.toString();
            
            ExecutableCommandResult cmdResult = new ExecutableCommandResult(result == 0);
            cmdResult.addArtifact(new StringArtifact("output", stdout));
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
    
    public class ExecutableCommandResult extends AbstractCommandResult
    {
        private boolean success;
        
        private List<Artifact> artifacts = new LinkedList<Artifact>();
        
        public ExecutableCommandResult(boolean succeeded)
        {
            this.success = succeeded;
        }
        
        public void addArtifact(Artifact artifact)
        {
            artifacts.add(artifact);
        }
        
        public boolean succeeded()
        {
            return success;
        }

        public List<Artifact> getArtifacts()
        {
            return Collections.unmodifiableList(artifacts);
        }
    }
}

package com.cinnamonbob.core;

import com.cinnamonbob.util.IOHelper;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A command that involves running an executable.
 */
public class ExecutableCommand implements Command
{

    private static final String EXE_OUTPUT_FILENAME               = "output.txt";
    
    private CommandCommon  common;
    private String         executable;
    private String[]       arguments;
    private File           workingDirectory;
    private ProcessBuilder builder;
    
    public void setCommand(List<String> command)
    {
        builder = new ProcessBuilder(command);
    }

    private int runChild(OutputStream outputStream) throws InternalBuildFailureException
    {
        try
        {
            Process        child       = builder.start();
            InputStream    childOutput = child.getInputStream();
            
            try
            {
                IOHelper.joinStreams(childOutput, outputStream);
            }
            catch(IOException e)
            {
                throw new InternalBuildFailureException("Error capturing child process output for command '" + common.getName() + "'", e);
            }
            
            return child.waitFor(); 
        }
        catch(IOException e)
        {
            // TODO should this be an internal failure? it is more a problem in the script...
            throw new InternalBuildFailureException("Error starting child process.", e);
        }
        catch(InterruptedException e)
        {
            // TODO Can we ever actually get interrupted?
            assert(false);
        }
        
        return -1;
    }
    
    
    private String constructCommandLine()
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


    public ExecutableCommand()
    {
    }
    
    public ExecutableCommandResult execute(File outputDir, BuildResult previousBuild) throws InternalBuildFailureException
    {
        builder.directory(getWorkingDirectory());
        builder.redirectErrorStream(true);

        File             outputFile = new File(outputDir, EXE_OUTPUT_FILENAME);
        FileOutputStream outputStream;
        int              result = -1;
        
        try
        {
            outputStream = new FileOutputStream(outputFile);
            result = runChild(outputStream);
        }
        catch(FileNotFoundException e)
        {
            throw new InternalBuildFailureException("Could not create command output file '" + outputFile.getAbsolutePath() + "'", e);
        }
        
        return new ExecutableCommandResult(constructCommandLine(), getWorkingDirectory().getAbsolutePath(), result);
    }
    
    
    public List<ArtifactSpec> getArtifacts()
    {
        List<ArtifactSpec> list = new LinkedList<ArtifactSpec>();
        
        list.add(new ArtifactSpec("output", "Command Output", Artifact.TYPE_PLAIN, new File(EXE_OUTPUT_FILENAME)));
        
        return list;
    }

    public String getExecutable()
    {
        return executable;
    }

    public void setExecutable(String executable)
    {
        this.executable = executable;
    }

    public String[] getArguments()
    {
        return arguments;
    }

    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }

    public void setCommandCommon(CommandCommon common)
    {
        this.common = common;
    }

    public void addEnv(String name, String value)
    {
        builder.environment().put(name, value);
    }
}

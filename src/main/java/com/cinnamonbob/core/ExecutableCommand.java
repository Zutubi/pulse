package com.cinnamonbob.core;

import nu.xom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A command that involves running an executable.
 */
public class ExecutableCommand implements Command
{
    private static final String CONFIG_NAME                   = "name";
    private static final String CONFIG_ATTR_EXECUTABLE        = "exe";
    private static final String CONFIG_ATTR_ARGUMENTS         = "args";
    private static final String CONFIG_ATTR_WORKING_DIRECTORY = "working-dir";
    
    private CommandCommon common;
    private String        executable;
    private String[]      arguments;
    private File          workingDirectory;
    
    
    public ExecutableCommand(String filename, Element element, CommandCommon common) throws ConfigException
    {
        this.common = common;
        loadConfig(filename, element);        
    }

    
    private void loadConfig(String filename, Element element) throws ConfigException
    {
        String working;
        
        executable = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_EXECUTABLE);
        working = element.getAttributeValue(CONFIG_ATTR_WORKING_DIRECTORY);
        if(working != null)
        {
            workingDirectory = new File(working);
        }
        
        arguments = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_ARGUMENTS).split(" ");
    }


    public ExecutableCommandResult execute(File outputDir) throws InternalBuildFailureException
    {
        List<String> command = new LinkedList<String>(Arrays.asList(arguments));
        command.add(0, executable);
        
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workingDirectory);
        builder.redirectErrorStream(true);

        File             outputFile = new File(outputDir, "output.txt");
        FileOutputStream outputStream;
        int              result = -1;
        
        try
        {
            outputStream = new FileOutputStream(outputFile);
            result = runChild(builder, outputStream);
        }
        catch(FileNotFoundException e)
        {
            throw new InternalBuildFailureException("Could not create command output file '" + outputFile.getAbsolutePath() + "'", e);
        }
        
        return new ExecutableCommandResult(result);
    }
    
    
    private int runChild(ProcessBuilder builder, OutputStream outputStream) throws InternalBuildFailureException
    {
        try
        {
            Process        child       = builder.start();
            InputStream    childOutput = child.getInputStream();
            
            try
            {
                joinStreams(childOutput, outputStream);
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


    private void joinStreams(InputStream input, OutputStream output) throws IOException
    {
        byte[] buffer = new byte[1024];
        int    n;
        
        while((n = input.read(buffer)) > 0)
        {
            output.write(buffer, 0, n);
        }
    }
}

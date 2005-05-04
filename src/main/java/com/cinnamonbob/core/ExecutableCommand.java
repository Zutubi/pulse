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

import com.cinnamonbob.util.IOHelper;

/**
 * A command that involves running an executable.
 */
public class ExecutableCommand implements Command
{
    private static final String CONFIG_ATTR_EXECUTABLE        = "exe";
    private static final String CONFIG_ATTR_ARGUMENTS         = "args";
    private static final String CONFIG_ATTR_WORKING_DIRECTORY = "working-dir";
    private static final String OUTPUT_FILENAME               = "output.txt";
    private static final String CONFIG_ELEMENT_ENVIRONMENT    = "environment";
    private static final String CONFIG_ATTR_NAME              = "name";
    private static final String CONFIG_ATTR_VALUE             = "value";
    
    private CommandCommon  common;
    private String         executable;
    private String[]       arguments;
    private File           workingDirectory;
    private ProcessBuilder builder;
    
    private void loadConfig(ConfigContext context, Element element) throws ConfigException
    {
        String working;
        
        executable = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_EXECUTABLE);
        working = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_WORKING_DIRECTORY, ".");
        workingDirectory = new File(working);
        
        String argumentString = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_ARGUMENTS, null);
        List<String> command;

        if(argumentString == null)
        {
            command = new LinkedList<String>();
        }
        else
        {
            arguments = argumentString.split(" ");
            command = new LinkedList<String>(Arrays.asList(arguments));
        }

        command.add(0, executable);
        builder = new ProcessBuilder(command);

        loadChildElements(context, element);
    }

    
    private void loadChildElements(ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList(CONFIG_ELEMENT_ENVIRONMENT));
        
        for(Element current: elements)
        {
            loadEnvironment(context, current);
        }        
    }

    
    private void loadEnvironment(ConfigContext context, Element element) throws ConfigException
    {
        String name  = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_NAME);
        String value = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_VALUE);
        
        builder.environment().put(name, value);
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


    public ExecutableCommand(ConfigContext context, Element element, CommandCommon common) throws ConfigException
    {
        this.common = common;
        loadConfig(context, element);        
    }

    
    public ExecutableCommandResult execute(File outputDir, BuildResult previousBuild) throws InternalBuildFailureException
    {
        builder.directory(workingDirectory);
        builder.redirectErrorStream(true);

        File             outputFile = new File(outputDir, OUTPUT_FILENAME);
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
        
        return new ExecutableCommandResult(constructCommandLine(), workingDirectory.getAbsolutePath(), result);
    }
    
    
    public List<ArtifactSpec> getArtifacts()
    {
        List<ArtifactSpec> list = new LinkedList<ArtifactSpec>();
        
        list.add(new ArtifactSpec("output", "Command Output", Artifact.TYPE_PLAIN, new File(OUTPUT_FILENAME)));
        
        return list;
    }
}

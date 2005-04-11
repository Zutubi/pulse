package com.cinnamonbob.core;

import nu.xom.Element;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.cinnamonbob.util.IOHelper;

/**
 * @author jsankey
 */
public class CommandCommon
{
    private static final Logger LOG = Logger.getLogger(CommandCommon.class.getName());
    
    private static final String CONFIG_ATTR_NAME        = "name";
    private static final String CONFIG_ELEMENT_ARTIFACT = "artifact";
    private static final String CONFIG_ELEMENT_PROCESS  = "process";
    
    private String name;
    private Command command;
    private Map<String, ArtifactSpec> artifacts;
    private List<ProcessSpec> processors;
    
    
    private void loadArtifact(String filename, Element element) throws ConfigException
    {
        ArtifactSpec spec = new ArtifactSpec(filename, element);

        if(artifacts.containsKey(spec.getName()))
        {
            throw new ConfigException(filename, "Command '" + name + "' already contains an artifact named '" + spec.getName() +"'");
        }
        artifacts.put(spec.getName(), spec);
    }

    
    private void loadProcess(String filename, Element element, Project project) throws ConfigException
    {
        processors.add(new ProcessSpec(filename, element, project, this));
    }


    private void collectArtifacts(CommandResultCommon commonResult, File outputDir)
    {
        for(ArtifactSpec artifactSpec: artifacts.values())
        {
            File toFile = artifactSpec.getToFile();
            
            if(!toFile.isAbsolute())
            {
                // Then it is relative to the output path.
                toFile = new File(outputDir, toFile.getName());
            }

            if(artifactSpec.requiresCollection())
            {
                File fromFile = artifactSpec.getFromFile();
                   
                try
                {
                    IOHelper.copyFile(fromFile, toFile);
                    commonResult.addArtifact(new Artifact(artifactSpec, toFile));
                }
                catch(IOException e)
                {
                    LOG.warning("I/O Error collecting artifact '" + artifactSpec.getName() + "': " + e.getMessage());
                }
            }
            else
            {
                if(toFile.isFile())
                {
                    commonResult.addArtifact(new Artifact(artifactSpec, toFile));
                }
                else
                {
                    LOG.warning("Omitting artifact '" + artifactSpec.getName() + "' as destination file was not created");
                }
            }
        }
    }

    
    private void postProcess(CommandResultCommon commonResult)
    {
        for(ProcessSpec processSpec: processors)
        {
            String artifactName = processSpec.getArtifact().getName();
            
            if(commonResult.hasArtifact(artifactName))
            {
                processSpec.getProcessor().process(commonResult.getArtifact(artifactName));
            }
            else
            {
                LOG.warning("Not applying post-processor '" + processSpec.getProcessor().getName() + "' due to missing artifact '" + artifactName + "'");
            }
        }
    }

    
    /**
     * @param filename
     * @param element
     * @param commandFactory
     * @param project
     * @throws ConfigException
     */
    public CommandCommon(String filename, Element element, CommandFactory commandFactory, Project project) throws ConfigException
    {
        name       = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_NAME);
        artifacts  = new TreeMap<String, ArtifactSpec>();
        processors = new LinkedList<ProcessSpec>();
        
        List<Element> childElements = XMLConfigUtils.getElements(filename, element);
        
        if(childElements.size() == 0)
        {
            throw new ConfigException(filename, "Command '" + name + "' contains no child elements.");
        }
        
        // The first child is the specific command element
        command = commandFactory.createCommand(childElements.get(0).getLocalName(), filename, childElements.get(0), this);
        
        for(ArtifactSpec artifactSpec: command.getArtifacts())
        {
            artifacts.put(artifactSpec.getName(), artifactSpec);
        }
        
        for(int i = 1; i < childElements.size(); i++)
        {
            Element child     = childElements.get(i);
            String  childName = child.getLocalName();
            
            if(childName.equals(CONFIG_ELEMENT_ARTIFACT))
            {
                loadArtifact(filename, child);
            }
            else if(childName.equals(CONFIG_ELEMENT_PROCESS))
            {
                loadProcess(filename, child, project);
            }
            else
            {
                throw new ConfigException(filename, "Command element includes unrecognised element '" + childName + "'");
            }
        }
    }
    

    /**
     * Returns the name of this command.
     * 
     * @return the name of this command
     */
    public String getName()
    {
        return name;
    }
    
    
    public boolean hasArtifact(String name)
    {
        return artifacts.containsKey(name);
    }
    
    
    public ArtifactSpec getArtifact(String name)
    {
        return artifacts.get(name);
    }

    
    public CommandResultCommon execute(File outputDir) throws InternalBuildFailureException
    {
        long                startTime     = System.currentTimeMillis();
        CommandResult       commandResult = command.execute(outputDir);
        CommandResultCommon commonResult  = new CommandResultCommon(name, commandResult, new TimeStamps(startTime, System.currentTimeMillis()));
        
        collectArtifacts(commonResult, outputDir);
        postProcess(commonResult);
        
        return commonResult;
    }
}

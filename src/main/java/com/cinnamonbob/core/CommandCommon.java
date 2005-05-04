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
import com.cinnamonbob.core.ext.ExtensionManager;
import com.cinnamonbob.core.ext.ExtensionManagerUtils;

/**
 * @author jsankey
 */
public class CommandCommon
{
    private static final Logger LOG = Logger.getLogger(CommandCommon.class.getName());
    
    private static final String CONFIG_ATTR_NAME        = "name";
    private static final String CONFIG_ATTR_FORCE       = "force";
    private static final String CONFIG_ELEMENT_ARTIFACT = "artifact";
    private static final String CONFIG_ELEMENT_PROCESS  = "process";
    
    /**
     * Decriptive name for the command.
     */
    private String name;
    /**
     * The command itself, to which execution is delegated.
     */
    private Command command;
    /**
     * Map from artifact name to specification for the artifact.
     */
    private Map<String, ArtifactSpec> artifacts;
    /**
     * Specifications for post-processors.
     */
    private List<ProcessSpec> processors;
    /**
     * If true, execute this command despite an earlier failure in the build.
     */
    private boolean force;
    
    //=======================================================================
    // Implementation
    //=======================================================================

    private void loadArtifact(ConfigContext context, Element element) throws ConfigException
    {
        ArtifactSpec spec = new ArtifactSpec(context, element);

        if(artifacts.containsKey(spec.getName()))
        {
            throw new ConfigException(context.getFilename(), "Command '" + name + "' already contains an artifact named '" + spec.getName() +"'");
        }
        artifacts.put(spec.getName(), spec);
    }

    
    private void loadProcess(ConfigContext context, Element element, Project project) throws ConfigException
    {
        processors.add(new ProcessSpec(context, element, project, this));
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
    
    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Constructs a new command by loading from the given element.
     * 
     * @param context
     *        configuration context used during loading
     * @param element
     *        root element of the command configuration
     * @param project
     *        the project this command belongs to
     * @throws ConfigException
     *         if there is an error in the configuration
     */
    public CommandCommon(ConfigContext context, Element element, Project project) throws ConfigException
    {
        name       = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_NAME);
        artifacts  = new TreeMap<String, ArtifactSpec>();
        processors = new LinkedList<ProcessSpec>();
        
        if(element.getAttributeValue(CONFIG_ATTR_FORCE) == null)
        {
            force = false;
        }
        else
        {
            force = true;
        }
        
        List<Element> childElements = XMLConfigUtils.getElements(context, element);
        
        if(childElements.size() == 0)
        {
            throw new ConfigException(context.getFilename(), "Command '" + name + "' contains no child elements.");
        }
        
        // The first child is the specific command element
        command = ExtensionManagerUtils.createCommand(childElements.get(0).getLocalName(), context, childElements.get(0), this);
        
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
                loadArtifact(context, child);
            }
            else if(childName.equals(CONFIG_ELEMENT_PROCESS))
            {
                loadProcess(context, child, project);
            }
            else
            {
                throw new ConfigException(context.getFilename(), "Command element includes unrecognised element '" + childName + "'");
            }
        }
    }
    
    //=======================================================================
    // Interface
    //=======================================================================

    /**
     * @return the name of this command
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns true if this command has a specification for the given artifact.
     * 
     * @param name
     *        the name to test for
     * @return true iff this command has an artifact of the given name
     */
    public boolean hasArtifact(String name)
    {
        return artifacts.containsKey(name);
    }
    
    /**
     * Returns a specification for the artifact of the given name.
     * 
     * @param name
     *        the name of the artifact to retrieve the specification for
     * @return the specification for the artifact of the given name, or null
     *         if there is none
     */
    public ArtifactSpec getArtifact(String name)
    {
        return artifacts.get(name);
    }

    /**
     * @return true iff this command should be executed despite an earlier
     *         failure
     */
    public boolean getForce()
    {
        return force;
    }
    
    /**
     * Executes this command, returning the result.
     * 
     * @param outputDir
     *        directory to store output produced by the command
     * @param previousBuild
     *        result of previous build or null if not available
     * @return the result of the command execution
     * @throws InternalBuildFailureException
     *         if an unexpected error occurs during execution
     */
    public CommandResultCommon execute(File outputDir, BuildResult previousBuild) throws InternalBuildFailureException
    {
        long                startTime     = System.currentTimeMillis();
        CommandResult       commandResult = command.execute(outputDir, previousBuild);
        CommandResultCommon commonResult  = new CommandResultCommon(name, commandResult, new TimeStamps(startTime, System.currentTimeMillis()));
        
        collectArtifacts(commonResult, outputDir);
        postProcess(commonResult);
        
        return commonResult;
    }
}

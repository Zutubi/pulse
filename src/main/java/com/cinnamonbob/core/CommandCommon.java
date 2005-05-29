package com.cinnamonbob.core;

import com.cinnamonbob.util.IOHelper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * @author jsankey
 */
public class CommandCommon
{
    private static final Logger LOG = Logger.getLogger(CommandCommon.class.getName());
    
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
    private Map<String, ArtifactSpec> artifacts= new TreeMap<String, ArtifactSpec>();;
    /**
     * Specifications for post-processors.
     */
    private List<ProcessSpec> processors = new LinkedList<ProcessSpec>();;
    /**
     * If true, initialise this command despite an earlier failure in the build.
     */
    private boolean force;
    
    //=======================================================================
    // Implementation
    //=======================================================================

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

    public CommandCommon()
    {        
    }
    
    public void setCommand(Command c)
    {
        this.command = c;
    }
    
    public void addArtifactSpec(ArtifactSpec spec)
    {
        if(artifacts.containsKey(spec.getName()))
        {
            throw new IllegalArgumentException("Artifact " + spec.getName() + " already exists.");
        }
        artifacts.put(spec.getName(), spec);
    }
    
    public void addProcessSpec(ProcessSpec spec)
    {
        processors.add(spec);
    }
    
    public void setForce(boolean b)
    {
        this.force = b;
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
    
    public void setName(String name)
    {
        this.name = name;
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

package com.cinnamonbob.core2.config;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 *
 */
public class CommandGroup implements Command
{
    private String name;
    
    private Command cmd = null;
    
    private List<ProcessArtifactMapping> mappings = new LinkedList<ProcessArtifactMapping>();
    
    private List<Artifact> artifacts = new LinkedList<Artifact>();
    
    public void add(Command cmd)
    {
        if (this.cmd != null)
        {
            throw new IllegalArgumentException("CommandGroup only supports a single command instance.");   
        }            
        this.cmd = cmd;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public ProcessArtifactMapping createProcess()
    {
        ProcessArtifactMapping mapping = new ProcessArtifactMapping();
        mappings.add(mapping);
        return mapping;
    }
    
    public Artifact createArtifact()
    {
        FileArtifact customArtifact = new FileArtifact();
        artifacts.add(customArtifact);
        return customArtifact;
    }
    
    public CommandResult execute() throws CommandException
    {        
        CommandResult result = cmd.execute();
        
        Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
        for (Artifact a : result.getArtifacts())
        {
            artifacts.put(a.getContentName(), a);
        }
        
        for (Artifact a : this.artifacts)
        {
            artifacts.put(a.getContentName(), a);
        }
           
        for (ProcessArtifactMapping m : mappings)
        {
            if (artifacts.containsKey(m.getArtifact()))
            {
                Artifact artifact = artifacts.get(m.getArtifact());
                m.getProcessor().process(artifact);
            }
            else
            {
                // unknown artifact.
            }
        }                
        return result;
    }    
    
    /**
     * 
     */ 
    private class ProcessArtifactMapping
    {
        private String artifact;
        private PostProcessor processor;

        public void setArtifact(String artifact)
        {
            this.artifact = artifact;
        }

        public void setProcessor(PostProcessor processor)
        {
            this.processor = processor;
        }

        public String getArtifact()
        {
            return artifact;
        }

        public PostProcessor getProcessor()
        {
            return processor;
        }
    }
}

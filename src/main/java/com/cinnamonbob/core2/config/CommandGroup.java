package com.cinnamonbob.core2.config;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.io.IOException;

import com.cinnamonbob.core.Artifact;
import com.cinnamonbob.core.ArtifactSpec;
import com.cinnamonbob.core.CommandResultCommon;
import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.util.IOHelper;
import com.cinnamonbob.util.TimeStamps;


/**
 * 
 *
 */
public class CommandGroup implements Command
{
    private String name;
    
    private Command cmd = null;
    
    private List<ProcessArtifactMapping> mappings = new LinkedList<ProcessArtifactMapping>();
    
    private List<FileArtifact> artifacts = new LinkedList<FileArtifact>();
    
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
    
    public FileArtifact createArtifact()
    {
        FileArtifact customArtifact = new FileArtifact();
        artifacts.add(customArtifact);
        return customArtifact;
    }
    
    public CommandResult execute(File outputDir) throws CommandException
    {
        TimeStamps    stamps = new TimeStamps();
        CommandResult result = cmd.execute(outputDir);
        
        stamps.end();
        result.setCommandName(name);
        result.setStamps(stamps);

        collectArtifacts(result, outputDir);
        
//        Map<String, FileArtifact> artifacts = new HashMap<String, FileArtifact>();
//        for (FileArtifact a : result.getArtifacts())
//        {
//            artifacts.put(a.getContentName(), a);
//        }
//        
//        for (FileArtifact a : this.artifacts)
//        {
//            artifacts.put(a.getContentName(), a);
//        }
//           
        for (ProcessArtifactMapping m : mappings)
        {
            StoredArtifact a = result.getArtifact(m.getArtifact());
            
            if(a != null)
            {
                m.getProcessor().process(a);
            }
            else
            {
                // unknown artifact.
            }
        }                
        return result;
    }
    
    private void collectArtifacts(CommandResult result, File outputDir)
    {
        for(FileArtifact artifact: artifacts)
        {
            File toFile = artifact.getToFile();
            
            if(toFile == null)
            {
                toFile = new File(artifact.getFile().getName());
            }
            
            if(!toFile.isAbsolute())
            {
                // Then it is relative to the output path.
                toFile = new File(outputDir, toFile.getName());
            }

            File fromFile = artifact.getFile();
                   
            try
            {
                IOHelper.copyFile(fromFile, toFile);
                result.addArtifact(new StoredArtifact(artifact, toFile.getAbsolutePath()));
            }
            catch(IOException e)
            {
                // TODO handle copy error
                e.printStackTrace();
            }
        }
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

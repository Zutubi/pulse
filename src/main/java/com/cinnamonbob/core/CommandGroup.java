package com.cinnamonbob.core;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.io.IOException;

import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.util.IOUtils;
import com.opensymphony.util.TextUtils;


/**
 * 
 *
 */
public class CommandGroup implements Command
{
    private String name;

    private Command command = null;

    private List<ProcessArtifactMapping> mappings = new LinkedList<ProcessArtifactMapping>();

    private List<FileArtifact> artifacts = new LinkedList<FileArtifact>();

    public void add(Command cmd) throws FileLoadException
    {
        if (this.command != null)
        {
            throw new FileLoadException("A 'command' tag may only contain a single nested command.");
        }
        this.command = cmd;

        if (!TextUtils.stringSet(this.command.getName()))
        {
            this.command.setName(name);
        }
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

    public void execute(File outputDir, CommandResult result)
    {
        command.execute(outputDir, result);
        collectArtifacts(result, outputDir);

        for (ProcessArtifactMapping m : mappings)
        {
            StoredArtifact a = result.getArtifact(m.getArtifact());

            if(a != null)
            {
                m.getProcessor().process(a);
            }
            else
            {
                 throw new BuildException("Unable to post-process unknown artifact '" + m.getArtifact() + "'");
            }
        }
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
                IOUtils.copyFile(fromFile, toFile);
                result.addArtifact(new StoredArtifact(artifact, toFile.getAbsolutePath()));
            }
            catch(IOException e)
            {
                throw new BuildException("Unable to collect artifact '" + artifact.getName() + "'", e);
            }
        }
    }

    public Command getCommand()
    {
        return command;
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

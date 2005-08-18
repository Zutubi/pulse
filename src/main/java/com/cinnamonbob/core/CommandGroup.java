package com.cinnamonbob.core;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.io.IOException;

import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.util.IOHelper;
import com.opensymphony.util.TextUtils;


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

        if (!TextUtils.stringSet(this.cmd.getName()))
        {
            this.cmd.setName(name);
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
        cmd.execute(outputDir, result);
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
                // TODO unknown artifact
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

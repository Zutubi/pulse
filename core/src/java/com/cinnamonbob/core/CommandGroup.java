package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.validation.Validateable;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.validator.ValidatorContext;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * 
 *
 */
public class CommandGroup implements Command, Validateable
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

    public void execute(File workDir, File outputDir, CommandResult result)
    {
        command.execute(workDir, outputDir, result);
        collectArtifacts(result, outputDir);

        for (ProcessArtifactMapping m : mappings)
        {
            StoredArtifact a = result.getArtifact(m.getArtifact());

            if (a != null)
            {
                m.getProcessor().process(outputDir, a);
            }
            else
            {
                throw new BuildException("Unable to post-process unknown artifact '" + m.getArtifact() + "'");
            }
        }
    }

    public List<String> getArtifactNames()
    {
        List<String> names = new LinkedList<String>();
        for (FileArtifact artifact : artifacts)
        {
            names.add(artifact.getName());
        }

        // dont forget about our nested friend. he may have some artifacts as well.
        names.addAll(getCommand().getArtifactNames());

        return names;
    }

    private void collectArtifacts(CommandResult result, File outputDir)
    {
        for (FileArtifact artifact : artifacts)
        {
            // the stored artifacts file name relative to the output directory.
            String relativeFileName = artifact.getName();

            File toFile = new File(outputDir, relativeFileName);

            File fromFile = artifact.getFile();

            try
            {
                IOUtils.copyFile(fromFile, toFile);
                result.addArtifact(new StoredArtifact(artifact, relativeFileName));
            }
            catch (IOException e)
            {
                throw new BuildException("Unable to collect artifact '" + artifact.getName() + "'", e);
            }
        }
    }

    public Command getCommand()
    {
        return command;
    }

    public void validate(ValidatorContext context)
    {
        // ensure that our artifacts have unique names.
        List<String> artifactNames = getArtifactNames();
        Set<String> names = new TreeSet<String>();
        for (String name : artifactNames)
        {
            if (names.contains(name))
            {
                context.addFieldError("name", "A duplicate artifact name '" + name + "' has been detected. Please only " +
                        "use unique names for artifacts within a command group.");
            }
            names.add(name);
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

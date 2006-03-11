package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.core.model.StoredFileArtifact;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 */
public abstract class Artifact
{
    private String name;
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();

    public Artifact(String name)
    {
        this.name = name;
    }

    public Artifact()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ProcessArtifact createProcess()
    {
        ProcessArtifact p = new ProcessArtifact();
        processes.add(p);
        return p;
    }

    protected void captureFile(StoredArtifact artifact, File fromFile, String path, File outputDir, CommandResult result, String type)
    {
        File toFile = new File(outputDir, path);
        File parent = toFile.getParentFile();

        try
        {
            FileSystemUtils.createDirectory(parent);
            IOUtils.copyFile(fromFile, toFile);
            StoredFileArtifact fileArtifact = new StoredFileArtifact(path, type);
            artifact.add(fileArtifact);
            result.addArtifact(artifact);

            for(ProcessArtifact process: processes)
            {
                process.getProcessor().process(outputDir, fileArtifact, result);
            }
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to collect file '" + fromFile.getAbsolutePath() + "' for artifact '" + getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Called to actually capture the artifacts from the working to the
     * output directory.
     *
     * @param result    command we are capturing artifacts from
     * @param baseDir   base directory for the project checkout
     * @param outputDir where to capture the artifacts to
     */
    public abstract void capture(CommandResult result, File baseDir, File outputDir);

}

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Name;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 * An artifact represents a by product of the build process that is considered important enough persist beyond the
 * lifecycle of a build.
 *
 * This Artifact class defines the base class for artifact element definitions that are used within the pulse file.
 *
 * For example:  <artifact name='myOutput' fail-if-not-present='false'/>
 *
 * The name of the artifact needs to be unique (todo: check the scope of the required uniquness...)
 */
public abstract class Artifact
{
    /**
     * The name of this artifact allows it to be referenced by name.
     */
    private String name;

    /**
     * If true, fail the command if the artifact cannot be captured.
     */
    private boolean failIfNotPresent = true;

    /**
     * The list of references to processors that will be applied to this artifact.
     *
     * These processors will later be responsible for extracting features from the artifact.
     */
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();

    public Artifact(String name)
    {
        this.name = name;
    }

    public Artifact()
    {
    }

    /**
     * Get the name of this artifact.
     *
     * @return the name of the artifact.
     */
    @Required @Name public String getName()
    {
        return name;
    }

    /**
     * Set the name of this artifact.
     *
     * @param name of this artifact.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the value of the fail if not present property.
     *
     * @return true if fail if not present is set to true, false otherwise.
     */
    public boolean getFailIfNotPresent()
    {
        return failIfNotPresent;
    }

    public void setFailIfNotPresent(boolean failIfNotPresent)
    {
        this.failIfNotPresent = failIfNotPresent;
    }

    /**
     * This is a factory method that allows artifact processors to be associated with the artifact.
     *
     * This allows the <process/> child element to be used with artifacts.
     *
     * @return the new instance of the processor reference.
     */
    public ProcessArtifact createProcess()
    {
        ProcessArtifact p = new ProcessArtifact();
        processes.add(p);
        return p;
    }

    /**
     * This method is a utility method available to handle the coping of an artifact into persistent storage and
     * running its post processors
     *
     * @param artifact  is the artifact entity to which the file belongs.
     * @param fromFile  is the source file. That is, the artifact file in the working directory.
     * @param path      is the path relative to the output directory to which the fromFile will be copied.
     * @param outputDir is the output directory into which the artifact (fromFile) will be copied
     * @param result    is the command result instance to which this artifact belongs. If processing of this artifact
     * identifies any error features, it is this command result that will be marked as failed.
     * @param type      is the mime type of the artifact.
     */
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
     * Called to actually capture the artifacts from the working to the output directory.
     *
     * @param result    command we are capturing artifacts from
     * @param baseDir   base directory for the project checkout
     * @param outputDir where to capture the artifacts to
     */
    public abstract void capture(CommandResult result, File baseDir, File outputDir);

}

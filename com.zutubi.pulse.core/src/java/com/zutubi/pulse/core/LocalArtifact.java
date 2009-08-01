package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * An artifact represents a by product of the build process that is considered important enough persist beyond the
 * lifecycle of a build.
 * <p/>
 * This LocalArtifact class defines the base class for artifact element definitions that are used within the pulse file.
 * <p/>
 * For example:  <artifact name='myOutput' fail-if-not-present='false'/>
 * <p/>
 * The name of the artifact needs to be unique (todo: check the scope of the required uniquness...)
 */
public abstract class LocalArtifact extends ArtifactSupport
{
    /**
     * If true, fail the command if the artifact cannot be captured.
     */
    private boolean failIfNotPresent = true;

    /**
     * If true, files with timestamps earlier than the recipe start time will
     * not be captured.
     */
    private boolean ignoreStale = false;

    /**
     * The list of references to processors that will be applied to this artifact.
     * <p/>
     * These processors will later be responsible for extracting features from the artifact.
     */
    private List<ProcessArtifact> processes = new LinkedList<ProcessArtifact>();

    public LocalArtifact(String name)
    {
        setName(name);
    }

    public LocalArtifact()
    {
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

    public boolean getIgnoreStale()
    {
        return ignoreStale;
    }

    public void setIgnoreStale(boolean ignoreStale)
    {
        this.ignoreStale = ignoreStale;
    }

    /**
     * This is a factory method that allows artifact processors to be associated with the artifact.
     * <p/>
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

    public void setProcesses(List<ProcessArtifact> processes)
    {
        this.processes = processes;
    }

    /**
     * This method is a utility method available to handle the coping of an artifact into persistent storage and
     * running its post processors
     *
     * @param artifact is the artifact entity to which the file belongs.
     * @param fromFile is the source file. That is, the artifact file in the working directory.
     * @param path     is the path relative to the output directory to which the fromFile will be copied.
     * @param result   is the command result instance to which this artifact belongs. If processing of this artifact
     *                 identifies any error features, it is this command result that will be marked as failed.
     * @param context  context for execution of the command
     * @param type     is the mime type of the artifact.
     * @return true if captured, false if ingored
     */
    protected boolean captureFile(StoredArtifact artifact, File fromFile, String path, CommandResult result, ExecutionContext context, String type)
    {
        if (ignoreStale && fromFile.lastModified() < context.getLong(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, 0))
        {
            return false;
        }

        File toFile = new File(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR), path);
        File parent = toFile.getParentFile();

        try
        {
            FileSystemUtils.createDirectory(parent);
            FileSystemUtils.copy(toFile, fromFile);
            StoredFileArtifact fileArtifact = new StoredFileArtifact(path, type);
            artifact.add(fileArtifact);

            processArtifact(fileArtifact, result, context, processes);

            return true;
        }
        catch (IOException e)
        {
            throw new BuildException("Unable to collect file '" + fromFile.getAbsolutePath() + "' for artifact '" + getName() + "': " + e.getMessage(), e);
        }
    }

    protected boolean isAbsolute(File f)
    {
        if (f.isAbsolute())
        {
            return true;
        }

        // On Windows File.isAbsolute() can return false for paths beginning
        // with a slash, although the path will act absolute in other ways.  So
        // we treat anything starting with a slash as absolute on Windows
        return SystemUtils.IS_WINDOWS && f.getPath().startsWith("/") || f.getPath().startsWith("\\");
    }

    protected void checkFailIfNotPresent(CommandResult result, String message)
    {
        if ((result.inProgress() || result.succeeded()) && getFailIfNotPresent())
        {
            result.error(message);
        }
    }
}

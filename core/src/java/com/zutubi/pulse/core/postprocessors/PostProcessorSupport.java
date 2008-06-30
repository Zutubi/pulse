package com.zutubi.pulse.core.postprocessors;

import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.SelfReference;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;

import java.io.File;

/**
 * <p>
 * A default implementation of {@link PostProcessor} which hides some
 * implementation details and provides utility methods for common cases.
 * This is still quite a low-level implementation, consider also more
 * targeted base classes.
 * </p>
 * <p>
 * This implementation handles standard fail on error and warning
 * capabilities, and hides the specifics of looking up the file to
 * process and adding features.
 * </p>
 *
 * @see TestReportPostProcessorSupport
 * @see XMLTestReportPostProcessorSupport
 * @see TextFilePostProcessorSupport
 * @see LineBasedPostProcessorSupport
 */
public abstract class PostProcessorSupport extends SelfReference implements PostProcessor
{
    /** @see #setFailOnError(boolean) */
    private boolean failOnError = true;
    /** @see #setFailOnWarning(boolean) */
    private boolean failOnWarning = false;

    /**
     * @see #setFailOnError(boolean)
     * @return current value of the fail on error flag
     */
    public boolean isFailOnError()
    {
        return failOnError;
    }

    /**
     * If set to true, the command (and thus build) will be failed when this
     * processor detects an error feature.  This flag is true by default.
     *
     * @param failOnError true to fail the build on error
     */
    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    /**
     * @see #setFailOnWarning(boolean)
     * @return current value of the fail on warning flag
     */
    public boolean isFailOnWarning()
    {
        return failOnWarning;
    }

    /**
     * If set to true, the command (and thus build) will be failed when this
     * processor detects a warning feature.  This flag is false by default.
     *
     * @param failOnWarning true to fail the build on error
     */
    public void setFailOnWarning(boolean failOnWarning)
    {
        this.failOnWarning = failOnWarning;
    }

    public void process(StoredFileArtifact artifact, CommandResult result, ExecutionContext context)
    {
        PostProcessorContext ppContext = new DefaultPostProcessorContext(artifact, result, context, failOnError, failOnWarning);
        File outputDir = context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR);
        process(new File(outputDir, artifact.getPath()), ppContext);
    }

    /**
     * Called once for each artifact file to process.  Details discovered by
     * processing should be added via the given context.
     *
     * @param artifactFile the file to post process
     * @param ppContext    context in which the post processing is executing
     */
    protected abstract void process(File artifactFile, PostProcessorContext ppContext);
}

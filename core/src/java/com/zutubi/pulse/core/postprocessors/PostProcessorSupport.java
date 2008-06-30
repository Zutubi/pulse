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
 * @see com.zutubi.pulse.core.postprocessors.TestReportPostProcessorSupport
 */
public abstract class PostProcessorSupport extends SelfReference implements PostProcessor
{
    private boolean failOnError = true;
    private boolean failOnWarning = false;

    public boolean isFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public boolean isFailOnWarning()
    {
        return failOnWarning;
    }

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

    protected abstract void process(File artifactFile, PostProcessorContext ppContext);
}

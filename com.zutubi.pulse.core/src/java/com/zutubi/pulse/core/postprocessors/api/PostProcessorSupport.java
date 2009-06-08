package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.engine.api.SelfReference;

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
 * capabilities.
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

    public final void process(File artifactFile, final PostProcessorContext ppContext)
    {
        processFile(artifactFile, new PostProcessorContext()
        {
            public ExecutionContext getExecutionContext()
            {
                return ppContext.getExecutionContext();
            }

            public ResultState getResultState()
            {
                return ppContext.getResultState();
            }

            public void addTests(TestSuiteResult suite, NameConflictResolution conflictResolution)
            {
                ppContext.addTests(suite, conflictResolution);
            }

            public void addFeature(Feature feature)
            {
                switch(feature.getLevel())
                {
                    case ERROR:
                        if (failOnError)
                        {
                            failCommand("Error features detected");
                        }
                        break;
                    case WARNING:
                        if (failOnWarning)
                        {
                            failCommand("Warning features detected");
                        }
                        break;
                }

                ppContext.addFeature(feature);
            }

            public void failCommand(String message)
            {
                ppContext.failCommand(message);
            }

            public void errorCommand(String message)
            {
                ppContext.errorCommand(message);
            }

            public void addFeatureToCommand(Feature feature)
            {
                ppContext.addFeatureToCommand(feature);
            }

            public void addCustomField(String name, String value)
            {
                ppContext.addCustomField(name, value);
            }
        });
    }

    protected abstract void processFile(File artifactFile, PostProcessorContext ppContext);
}

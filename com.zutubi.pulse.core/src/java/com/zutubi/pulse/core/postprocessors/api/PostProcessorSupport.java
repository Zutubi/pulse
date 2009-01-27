package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResultState;

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
public abstract class PostProcessorSupport implements PostProcessor
{
    private PostProcessorConfigurationSupport config;

    protected PostProcessorSupport(PostProcessorConfigurationSupport config)
    {
        this.config = config;
    }

    public PostProcessorConfigurationSupport getConfig()
    {
        return config;
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
                        if (config.isFailOnError())
                        {
                            failCommand("Error features detected");
                        }
                        break;
                    case WARNING:
                        if (config.isFailOnWarning())
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
        });
    }

    protected abstract void processFile(File artifactFile, PostProcessorContext ppContext);
}

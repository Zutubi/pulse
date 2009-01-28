package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * <p>
 * A default implementation of {@link com.zutubi.pulse.core.postprocessors.api.PostProcessor} which hides some
 * implementation details and provides utility methods for common cases.
 * This is still quite a low-level implementation, consider also more
 * targeted base classes.
 * </p>
 * <p>
 * This implementation handles standard fail on error and warning
 * capabilities.
 * </p>
 *
 * @see com.zutubi.pulse.core.postprocessors.api.TestReportPostProcessorSupport
 * @see com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorSupport
 * @see com.zutubi.pulse.core.postprocessors.api.TextFilePostProcessorSupport
 * @see com.zutubi.pulse.core.postprocessors.api.LineBasedPostProcessorSupport
 */
@SymbolicName("zutubi.postProcessorConfigSupport")
public abstract class PostProcessorConfigurationSupport extends AbstractNamedConfiguration implements PostProcessorConfiguration
{
    private Class<? extends PostProcessor> postProcessorType;
    /** @see #setFailOnError(boolean) */
    private boolean failOnError = true;
    /** @see #setFailOnWarning(boolean) */
    private boolean failOnWarning = false;

    protected PostProcessorConfigurationSupport(Class<? extends PostProcessor> postProcessorType)
    {
        this.postProcessorType = postProcessorType;
    }

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

    public Class<? extends PostProcessor> processorType()
    {
        return postProcessorType;
    }
}
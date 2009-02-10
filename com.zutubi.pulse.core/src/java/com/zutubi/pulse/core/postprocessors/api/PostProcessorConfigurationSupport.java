package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Support base class to configure an instance of {@link PostProcessorSupport}.
 */
@SymbolicName("zutubi.postProcessorConfigSupport")
public abstract class PostProcessorConfigurationSupport extends AbstractNamedConfiguration implements PostProcessorConfiguration
{
    private Class<? extends PostProcessor> postProcessorType;
    @Wizard.Ignore
    private boolean failOnError = true;
    @Wizard.Ignore
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
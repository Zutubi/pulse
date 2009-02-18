package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;

/**
 * Helper base class for configuration of processors that find features in
 * output.
 */
@SymbolicName("zutubi.outputPostProcessorConfigSupport")
public abstract class OutputPostProcessorConfigurationSupport extends PostProcessorConfigurationSupport
{
    @Wizard.Ignore
    private boolean failOnError = true;
    @Wizard.Ignore
    private boolean failOnWarning = false;

    protected OutputPostProcessorConfigurationSupport(Class<? extends PostProcessor> postProcessorType)
    {
        super(postProcessorType);
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
}

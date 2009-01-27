package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.SelfReference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

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
public abstract class PostProcessorConfigurationSupport extends SelfReference implements PostProcessorConfiguration
{
    private static final Logger LOG = Logger.getLogger(PostProcessorConfigurationSupport.class);

    private Class<? extends PostProcessor> postProcessorType;
    /** @see #setFailOnError(boolean) */
    private boolean failOnError = true;
    /** @see #setFailOnWarning(boolean) */
    private boolean failOnWarning = false;

    @Transient
    private ObjectFactory objectFactory;

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

    protected <T extends PostProcessor> T buildPostProcessor(Class<T> postProcessorType, Class<? extends PostProcessorConfiguration> configType, PostProcessorConfiguration config)
    {
        try
        {
            return objectFactory.buildBean(postProcessorType, new Class[]{configType}, new Object[]{config});
        }
        catch (Exception e)
        {
            LOG.severe(e);
            throw new BuildException("Unable to instantiate post-processor '" + getName() + "': " + e.getMessage(), e);
        }
    }

    public PostProcessor createProcessor()
    {
        return buildPostProcessor(postProcessorType, getClass(), this);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
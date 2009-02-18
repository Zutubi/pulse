package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;
import com.zutubi.util.ReflectionUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Constructor;

/**
 * Default implementation of {@link com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory},
 * which uses the object factory to build processors.
 */
public class DefaultPostProcessorFactory implements PostProcessorFactory
{
    private static final Logger LOG = Logger.getLogger(DefaultPostProcessorFactory.class);

    private ObjectFactory objectFactory;

    public PostProcessor createProcessor(PostProcessorConfiguration configuration)
    {
        // We need to pass the formal argument type to the object factory, so
        // figure it out from the actual type of the configuration.
        Class<?> formalParameterType = null;
        Class<? extends PostProcessor> type = configuration.processorType();
        for (Constructor constructor: type.getConstructors())
        {
            if (ReflectionUtils.acceptsParameters(constructor, configuration.getClass()))
            {
                formalParameterType = constructor.getParameterTypes()[0];
            }
        }

        if (formalParameterType == null)
        {
            throw new BuildException("Unable to create post-processor '" + configuration.getName() + "': No constructor of post-processor type '" + type.getName() + "' accepts configuration of type '" + configuration.getClass().getName() + "'");
        }

        try
        {
            return objectFactory.buildBean(type, new Class[]{formalParameterType}, new Object[]{configuration});
        }
        catch (Exception e)
        {
            LOG.severe(e);
            throw new BuildException("Unable to instantiate post-processor '" + configuration.getName() + "': " + e.getMessage(), e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}

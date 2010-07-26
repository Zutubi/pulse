package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A version of a project that collects referenceable elements like
 * post-processors.  This is useful for building documention and/or examples,
 * as the standard project recipes configuration does not remember references
 * (it cannot be done in general due to scoping).
 */
@SymbolicName("zutubi.referenceCollectionProjectRecipesConfig")
public class ReferenceCollectingProjectRecipesConfiguration extends ProjectRecipesConfiguration
{
    private Map<String, PostProcessorConfiguration> postProcessors = new LinkedHashMap<String, PostProcessorConfiguration>();
    private Map<String, PropertyConfiguration> properties = new HashMap<String, PropertyConfiguration>();

    public Map<String, PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(Map<String, PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        postProcessors.put(postProcessor.getName(), postProcessor);
    }

    public Map<String, PropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, PropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public void addProperty(PropertyConfiguration property)
    {
        properties.put(property.getName(), property);
    }
}

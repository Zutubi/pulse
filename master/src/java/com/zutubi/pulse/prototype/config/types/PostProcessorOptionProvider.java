package com.zutubi.pulse.prototype.config.types;

import com.zutubi.prototype.MapOptionProvider;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.PostProcessorManager;

import java.util.Map;

/**
 *
 *
 */
public class PostProcessorOptionProvider extends MapOptionProvider
{
    private PostProcessorManager postProcessorManager;

    protected Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        return postProcessorManager.getAvailableProcessors();
    }

    public void setPostProcessorManager(PostProcessorManager postProcessorManager)
    {
        this.postProcessorManager = postProcessorManager;
    }
}

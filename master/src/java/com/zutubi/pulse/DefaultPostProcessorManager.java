package com.zutubi.pulse;

import com.zutubi.pulse.core.plugins.PostProcessorDescriptor;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import com.zutubi.pulse.prototype.config.project.types.DefaultPostProcessorFragment;
import com.zutubi.pulse.prototype.config.project.types.PostProcessorFragment;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DefaultPostProcessorManager implements PostProcessorManager
{
    private PostProcessorExtensionManager postProcessorExtensionManager;

    public PostProcessorFragment getProcessor(String name)
    {
        return getAvailableProcessors().get(name);
    }
    
    public Map<String, PostProcessorFragment> getAvailableProcessors()
    {
        Map<String, PostProcessorFragment> result = new TreeMap<String, PostProcessorFragment>();
        Collection<PostProcessorDescriptor> postProcessors = postProcessorExtensionManager.getPostProcessors();
        for(PostProcessorDescriptor descriptor: postProcessors)
        {
            if(descriptor.isDefaultFragment())
            {
                String name = descriptor.getName();
                if(name.endsWith(".pp"))
                {
                    name = name.substring(0, name.length() - 3);
                }
                result.put(name, new DefaultPostProcessorFragment(name, descriptor.getDisplayName()));
            }
        }

        return result;
    }

    public void setPostProcessorExtensionManager(PostProcessorExtensionManager postProcessorExtensionManager)
    {
        this.postProcessorExtensionManager = postProcessorExtensionManager;
    }
}

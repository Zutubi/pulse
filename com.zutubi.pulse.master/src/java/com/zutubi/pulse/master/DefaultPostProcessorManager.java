package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.plugins.PostProcessorDescriptor;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.DefaultPostProcessorFragment;
import com.zutubi.pulse.master.tove.config.project.types.PostProcessorFragment;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DefaultPostProcessorManager implements PostProcessorManager, EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultPostProcessorManager.class);

    private PostProcessorExtensionManager postProcessorExtensionManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public void init()
    {
        TemplateHierarchy templateHierarchy = configurationTemplateManager.getTemplateHierarchy(MasterConfigurationRegistry.PROJECTS_SCOPE);
        String globalProjectName = templateHierarchy.getRoot().getId();
        ProjectConfiguration globalProject = (ProjectConfiguration) configurationTemplateManager.getInstance(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, globalProjectName));
        boolean changed = false;
        Map<String, PostProcessorConfiguration> postProcessors = globalProject.getPostProcessors();

        for (PostProcessorDescriptor descriptor: postProcessorExtensionManager.getPostProcessors())
        {
            if (descriptor.isDefaultFragment())
            {
                if (!postProcessors.containsKey(descriptor.getDisplayName()))
                {
                    if (!changed)
                    {
                        changed = true;
                        globalProject = configurationTemplateManager.deepClone(globalProject);
                        postProcessors = globalProject.getPostProcessors();
                    }

                    addDefaultProcessor(postProcessors, descriptor);
                }
            }
        }

        if (changed)
        {
            configurationTemplateManager.save(globalProject);
        }
    }

    private void addDefaultProcessor(Map<String, PostProcessorConfiguration> postProcessors, PostProcessorDescriptor descriptor)
    {
        try
        {
            Class<? extends PostProcessorConfiguration> clazz = descriptor.getClazz();
            PostProcessorConfiguration processor = clazz.newInstance();
            processor.setName(descriptor.getDisplayName());
            postProcessors.put(processor.getName(), processor);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to add default " + descriptor.getName() + " processor: " + e.getMessage(), e);
        }
    }

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

    public void handleEvent(Event event)
    {
        init();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ SystemStartedEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void setPostProcessorExtensionManager(PostProcessorExtensionManager postProcessorExtensionManager)
    {
        this.postProcessorExtensionManager = postProcessorExtensionManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}

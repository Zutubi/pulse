package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.plugins.PostProcessorDescriptor;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.util.logging.Logger;

import java.util.Map;

/**
 * Manages post-processors on the master, e.g. contributes default processors
 * to the global project template.
 */
public class PostProcessorManager implements EventListener
{
    private static final Logger LOG = Logger.getLogger(PostProcessorManager.class);

    private PostProcessorExtensionManager postProcessorExtensionManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public void init()
    {
        ProjectConfiguration globalProject = configurationTemplateManager.getRootInstance(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectConfiguration.class);
        boolean changed = false;
        Map<String, PostProcessorConfiguration> postProcessors = globalProject.getPostProcessors();

        for (PostProcessorDescriptor descriptor: postProcessorExtensionManager.getPostProcessors())
        {
            if (descriptor.isContributeDefault())
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

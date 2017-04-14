/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages post-processors on the master, e.g. contributes default processors
 * to the global project template.
 */
public class PostProcessorManager implements EventListener
{
    private static final Logger LOG = Logger.getLogger(PostProcessorManager.class);

    private static final Set<String> KNOWN_DEFAULT_PROCESSOR_KEYS = new HashSet<String>(Arrays.asList(
        "ant output processor",
        "boost.test XML report processor",
        "boost jam output processor",
        "boost regression xml report processor",
        "cppunit xml report processor",
        "cunit xml report processor",
        "custom field processor",
        "gcc output processor",
        "junitee xml report processor",
        "junit summary output processor",
        "junit xml report processor",
        "make output processor",
        "maven 1 output processor",
        "maven 2 output processor",
        "maven 3 output processor",
        "msbuild output processor",
        "nant output processor",
        "nunit xml report processor",
        "ocunit output processor",
        "qtestlib xml report processor",
        "unittest++ xml report processor",
        "visual studio output processor",
        "xcodebuild output processor"
    ));
    
    private PostProcessorExtensionManager postProcessorExtensionManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private RecordManager recordManager;
    private TypeRegistry typeRegistry;

    public void init()
    {
        ProjectConfiguration globalProject = configurationTemplateManager.getRootInstance(MasterConfigurationRegistry.PROJECTS_SCOPE, ProjectConfiguration.class);

        cleanupProcessorsOfUnknownType(globalProject.getConfigurationPath());
        
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

    private void cleanupProcessorsOfUnknownType(String globalProjectPath)
    {
        // A hack to work around CIB-2638 and CIB-2648.  Because we do not have
        // good handling of configuration of unknown type (usually caused by
        // missing plugins), instead we just wipe out any default
        // post-processors for types we don't recognise.  This means the user
        // won't be bitten by disabling a plugin that is never explicitly used
        // by them, which hopefully covers a lot of cases where the user
        // deliberately removes a plugin.
        String postProcessorsPath = PathUtils.getPath(globalProjectPath, "postProcessors");
        Record record = recordManager.select(postProcessorsPath);
        for (String key: record.nestedKeySet())
        {
            if (KNOWN_DEFAULT_PROCESSOR_KEYS.contains(key))
            {
                Record child = (Record) record.get(key);
                if (typeRegistry.getType(child.getSymbolicName()) == null)
                {
                    String childPath = PathUtils.getPath(postProcessorsPath, key);
                    LOG.warning("Deleting post-processor at '" + childPath + "' as it has unknown type '" + child.getSymbolicName() + "'");
                    configurationTemplateManager.delete(childPath);
                }
            }
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

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

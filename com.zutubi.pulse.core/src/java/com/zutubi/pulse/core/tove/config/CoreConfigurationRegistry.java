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

package com.zutubi.pulse.core.tove.config;

import com.zutubi.pulse.core.commands.CommandGroupConfiguration;
import com.zutubi.pulse.core.commands.api.*;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.ReferenceCollectingProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.logging.Logger;


/**
 * Simple implementation of {@link ConfigurationRegistry} that defers to the
 * {@link com.zutubi.tove.type.TypeRegistry}.  Registers types used by all
 * components.
 */
public class CoreConfigurationRegistry implements ConfigurationRegistry
{
    private static final Logger LOG = Logger.getLogger(CoreConfigurationRegistry.class);

    protected TypeRegistry typeRegistry;

    public void init()
    {
        try
        {
            registerConfigurationType(ProjectRecipesConfiguration.class);
            registerConfigurationType(ReferenceCollectingProjectRecipesConfiguration.class);
            registerConfigurationType(PropertyConfiguration.class);
            registerConfigurationType(ArtifactConfiguration.class);
            registerConfigurationType(ArtifactConfigurationSupport.class);
            registerConfigurationType(FileSystemArtifactConfigurationSupport.class);
            registerConfigurationType(DirectoryArtifactConfiguration.class);
            registerConfigurationType(FileArtifactConfiguration.class);
            registerConfigurationType(LinkArtifactConfiguration.class);

            registerConfigurationType(PostProcessorConfiguration.class);
            registerConfigurationType(PostProcessorConfigurationSupport.class);
            registerConfigurationType(OutputPostProcessorConfigurationSupport.class);
            registerConfigurationType(TextFilePostProcessorConfigurationSupport.class);
            registerConfigurationType(LineBasedPostProcessorConfigurationSupport.class);
            registerConfigurationType(TestReportPostProcessorConfigurationSupport.class);
            registerConfigurationType(XMLTestReportPostProcessorConfigurationSupport.class);

            registerConfigurationType(CommandConfiguration.class);
            registerConfigurationType(CommandConfigurationSupport.class);
            registerConfigurationType(OutputProducingCommandConfigurationSupport.class);
            registerConfigurationType(CommandGroupConfiguration.class);

            registerConfigurationType(ResourcesConfiguration.class);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public <T extends Configuration> CompositeType registerConfigurationType(Class<T> clazz) throws TypeException
    {
        return typeRegistry.register(clazz);
    }

    @Override
    public CompositeType getConfigurationCheckType(CompositeType type)
    {
        // TODO we really could handle this here, or even in a lower implementation in tove.ui.
        return null;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

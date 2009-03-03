package com.zutubi.pulse.core.tove.config;

import com.zutubi.pulse.core.ResourcesConfiguration;
import com.zutubi.pulse.core.commands.api.*;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.postprocessors.api.*;
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
            registerConfigurationType(Property.class);
            registerConfigurationType(OutputConfiguration.class);
            registerConfigurationType(OutputConfigurationSupport.class);
            registerConfigurationType(FileSystemOutputConfigurationSupport.class);
            registerConfigurationType(DirectoryOutputConfiguration.class);
            registerConfigurationType(FileOutputConfiguration.class);
            registerConfigurationType(LinkOutputConfiguration.class);

            registerConfigurationType(PostProcessorConfiguration.class);
            registerConfigurationType(PostProcessorConfigurationSupport.class);
            registerConfigurationType(OutputPostProcessorConfigurationSupport.class);
            registerConfigurationType(TextFilePostProcessorConfigurationSupport.class);
            registerConfigurationType(LineBasedPostProcessorConfigurationSupport.class);
            registerConfigurationType(TestReportPostProcessorConfigurationSupport.class);
            registerConfigurationType(XMLTestReportPostProcessorConfigurationSupport.class);

            registerConfigurationType(CommandConfigurationSupport.class);
            registerConfigurationType(OutputProducingCommandConfigurationSupport.class);

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

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

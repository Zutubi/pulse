package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.MasterBuildProperties;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.variables.ConfigurationVariableProvider;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

/**
 * Constructs a variable map for a configuration instance depending on where it lives.  This is a
 * highly specialised implementation the hopefully will be generalised.  To do so requires some
 * thought on performance, e.g. the maps may need some caching.
 */
public class MasterConfigurationVariableProvider implements ConfigurationVariableProvider
{
    private static final Logger LOG = Logger.getLogger(MasterConfigurationVariableProvider.class);
    
    private ConfigurationProvider configurationProvider;
    private TypeRegistry typeRegistry;
    
    public VariableMap variablesForConfiguration(Configuration config)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        if (StringUtils.stringSet(config.getConfigurationPath()))
        {
            ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(config, ProjectConfiguration.class);
            if (projectConfig != null)
            {
                MasterBuildProperties.addProjectProperties(context, projectConfig);
                for (ResourcePropertyConfiguration property: projectConfig.getProperties().values())
                {
                    context.add(property.asResourceProperty());
                }
            }
        }

        return context.getScope();
    }

    @SuppressWarnings("unchecked")
    public <T extends Configuration> T resolveStringProperties(T config)
    {
        VariableMap variables = variablesForConfiguration(config);
        if (variables.getVariables().isEmpty())
        {
            return config;
        }
        
        Class<? extends Configuration> clazz = config.getClass();
        try
        {
            CompositeType type = typeRegistry.getType(clazz);
            if (type != null)
            {
                Configuration resolvedConfig = clazz.newInstance();
                resolveProperties(variables, type, config, resolvedConfig);
                config = (T) resolvedConfig;
            }            
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }

        return config;
    }

    private <T extends Configuration> void resolveProperties(VariableMap variables, CompositeType type, T config, Configuration resolvedConfig) throws Exception
    {
        for (TypeProperty property: type.getProperties())
        {
            Type propertyType = property.getType();
            Object value = property.getValue(config);
            if (value != null && propertyType instanceof SimpleType && type.getClazz().equals(String.class))
            {
                value = VariableResolver.safeResolveVariables((String) value, variables);
            }
            property.setValue(resolvedConfig, value);
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

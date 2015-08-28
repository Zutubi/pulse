package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.MasterBuildProperties;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.variables.ConfigurationVariableProvider;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.logging.Logger;

/**
 * Constructs a variable map for a configuration instance depending on where it lives.  This is a
 * highly specialised implementation the hopefully will be generalised.  To do so requires some
 * thought on performance, e.g. the maps may need some caching.
 */
public class MasterConfigurationVariableProvider implements ConfigurationVariableProvider
{
    private static final Logger LOG = Logger.getLogger(MasterConfigurationVariableProvider.class);
    
    private TypeRegistry typeRegistry;
    
    public VariableMap variablesForConfiguration(Configuration scope)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        if (scope != null && scope instanceof ProjectConfiguration)
        {
            ProjectConfiguration projectConfig = (ProjectConfiguration) scope;
            MasterBuildProperties.addProjectProperties(context, projectConfig, true);
        }

        return context.getScope();
    }

    @SuppressWarnings("unchecked")
    public <T extends Configuration> T resolveStringProperties(T config, VariableMap variables)
    {
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
            if (value != null && propertyType instanceof PrimitiveType)
            {
                PrimitiveType primitiveType = (PrimitiveType) propertyType;
                String stringValue = primitiveType.unstantiate(value, null);
                stringValue = VariableResolver.safeResolveVariables(stringValue, variables);
                value = primitiveType.instantiate(stringValue, null);
            }
            property.setValue(resolvedConfig, value);
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}

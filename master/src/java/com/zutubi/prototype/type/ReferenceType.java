package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.pulse.core.config.Configuration;

/**
 * A type that represents a reference to some composite type.  The reference
 * value itself is just a path, which resolves to a record stored at that
 * location.
 */
public class ReferenceType extends SimpleType implements Type
{
    private CompositeType referencedType;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public ReferenceType(CompositeType referencedType, ConfigurationPersistenceManager configurationPersistenceManager)
    {
        super(referencedType.getClazz());
        this.referencedType = referencedType;
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public CompositeType getReferencedType()
    {
        return referencedType;
    }

    public Object instantiate(String path, Object data) throws TypeException
    {
        String referencePath = (String) data;
        if(referencePath.length() > 0)
        {
            return configurationPersistenceManager.resolveReference(path, referencePath);
        }
        else
        {
            // Empty string == null reference.
            return null;
        }
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        return ((Configuration)instance).getConfigurationPath();
    }
}

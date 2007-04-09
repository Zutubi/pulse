package com.zutubi.prototype.type;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

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
        return configurationPersistenceManager.getInstance(referencePath);
    }
}

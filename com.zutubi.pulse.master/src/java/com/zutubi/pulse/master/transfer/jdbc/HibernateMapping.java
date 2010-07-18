package com.zutubi.pulse.master.transfer.jdbc;

import org.hibernate.MappingException;
import org.hibernate.id.factory.DefaultIdentifierGeneratorFactory;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.type.Type;

/**
 *
 *
 */
public class HibernateMapping implements Mapping
{
    private final Configuration configuration;
    private IdentifierGeneratorFactory identifierGeneratorFactory = new DefaultIdentifierGeneratorFactory();

    public HibernateMapping(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public Type getIdentifierType(String persistentClass) throws MappingException
    {
        PersistentClass pc = configuration.getClassMapping(persistentClass);
        if (pc == null)
        {
            throw new MappingException("persistent class not known: " + persistentClass);
        }
        return pc.getIdentifier().getType();
    }

    public String getIdentifierPropertyName(String persistentClass) throws MappingException
    {
        final PersistentClass pc = configuration.getClassMapping(persistentClass);
        if (pc == null)
        {
            throw new MappingException("persistent class not known: " + persistentClass);
        }
        if (!pc.hasIdentifierProperty()) return null;
        return pc.getIdentifierProperty().getName();
    }

    public Type getReferencedPropertyType(String persistentClass, String propertyName) throws MappingException
    {
        final PersistentClass pc = configuration.getClassMapping(persistentClass);
        if (pc == null)
        {
            throw new MappingException("persistent class not known: " + persistentClass);
        }
        Property prop = pc.getReferencedProperty(propertyName);
        if (prop == null)
        {
            throw new MappingException(
                    "property not known: " +
                            persistentClass + '.' + propertyName
            );
        }
        return prop.getType();
    }

    public IdentifierGeneratorFactory getIdentifierGeneratorFactory()
    {
        return identifierGeneratorFactory;
    }
}

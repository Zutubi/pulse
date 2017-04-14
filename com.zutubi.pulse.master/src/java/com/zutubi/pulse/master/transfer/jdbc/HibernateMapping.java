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

package com.zutubi.pulse.master.transfer.jdbc;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
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

    public HibernateMapping(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public IdentifierGeneratorFactory getIdentifierGeneratorFactory()
    {
        return null;
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
}

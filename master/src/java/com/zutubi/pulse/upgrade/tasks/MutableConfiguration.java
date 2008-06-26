package com.zutubi.pulse.upgrade.tasks;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class MutableConfiguration extends Configuration
{
    public void addTable(Table table)
    {
        String key = getTableKey(table);
        if (tables.containsKey(key))
        {
            throw new IllegalArgumentException();
        }
        tables.put(key, table);
    }

    private String getTableKey(Table table)
    {
        return table.getSubselect() == null ?
                Table.qualify(table.getCatalog(), table.getSchema(), table.getName()) :
                table.getSubselect();
    }

    public void removeTable(Table table)
    {
        String key = getTableKey(table);
        if(tables.remove(key) == null)
        {
            throw new IllegalArgumentException();
        }
    }

    public Mapping getMapping() {
        return new Mapping() {
            /**
             * Returns the identifier type of a mapped class
             */
            public Type getIdentifierType(String persistentClass) throws MappingException
            {
                PersistentClass pc = ( (PersistentClass) classes.get( persistentClass ) );
                if ( pc == null ) {
                    throw new MappingException( "persistent class not known: " + persistentClass );
                }
                return pc.getIdentifier().getType();
            }

            public String getIdentifierPropertyName(String persistentClass) throws MappingException {
                final PersistentClass pc = (PersistentClass) classes.get( persistentClass );
                if ( pc == null ) {
                    throw new MappingException( "persistent class not known: " + persistentClass );
                }
                if ( !pc.hasIdentifierProperty() ) return null;
                return pc.getIdentifierProperty().getName();
            }

            public Type getReferencedPropertyType(String persistentClass, String propertyName) throws MappingException {
                final PersistentClass pc = (PersistentClass) classes.get( persistentClass );
                if ( pc == null ) {
                    throw new MappingException( "persistent class not known: " + persistentClass );
                }
                Property prop = pc.getReferencedProperty( propertyName );
                if ( prop == null ) {
                    throw new MappingException(
                            "property not known: " +
                            persistentClass + '.' + propertyName
                        );
                }
                return prop.getType();
            }
        };
    }

    public void addFileSystemMappings(List<File> mappings) throws IOException
    {
        List<Resource> resources = new LinkedList<Resource>();
        for (File file : mappings)
        {
            resources.add(new FileSystemResource(file));
        }
        addMappings(resources);
    }

    public void addClassPathMappings(List<String> mappings) throws IOException
    {
        List<Resource> resources = new LinkedList<Resource>();
        for (String mapping : mappings)
        {
            resources.add(new ClassPathResource(mapping));
        }
        addMappings(resources);
    }

    public void addMappings(List<Resource> mappings) throws IOException
    {
        for (Resource resource : mappings)
        {
            addInputStream(resource.getInputStream());
        }
    }
}

package com.zutubi.pulse.master.hibernate;

import com.zutubi.i18n.Messages;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
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
import java.util.Properties;

/**
 * A mutable implementation of the {@link org.hibernate.cfg.Configuration}.  The
 * default implementation is immutable, making it difficult to 'refactor' the schema
 * at runtime.
 */
public class MutableConfiguration extends Configuration
{
    private static final Messages I18N = Messages.getInstance(MutableConfiguration.class);

    private List<Resource> resources = new LinkedList<Resource>();
    private Properties properties = new Properties();
    private List<Table> tabs = new LinkedList<Table>();

    public void addTable(Table table)
    {
        String key = getTableKey(table);
        if (tables.containsKey(key))
        {
            throw new IllegalArgumentException(I18N.format("table.exists", key));
        }
        tabs.add(table);
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
        if (tables.remove(key) == null)
        {
            throw new IllegalArgumentException(I18N.format("table.does.not.exist", key));
        }
        tabs.remove(table);
    }

    public Mapping getMapping()
    {
        return new Mapping()
        {
            public Type getIdentifierType(String persistentClass) throws MappingException
            {
                PersistentClass pc = ((PersistentClass) classes.get(persistentClass));
                if (pc == null)
                {
                    throw new MappingException(I18N.format("unknown.persistent.class", pc));
                }
                return pc.getIdentifier().getType();
            }

            public String getIdentifierPropertyName(String persistentClass) throws MappingException
            {
                final PersistentClass pc = (PersistentClass) classes.get(persistentClass);
                if (pc == null)
                {
                    throw new MappingException(I18N.format("unknown.persistent.class", pc));
                }
                if (!pc.hasIdentifierProperty()) return null;
                return pc.getIdentifierProperty().getName();
            }

            public Type getReferencedPropertyType(String persistentClass, String propertyName) throws MappingException
            {
                final PersistentClass pc = (PersistentClass) classes.get(persistentClass);
                if (pc == null)
                {
                    throw new MappingException(I18N.format("unknown.persistent.class", pc));
                }
                Property prop = pc.getReferencedProperty(propertyName);
                if (prop == null)
                {
                    throw new MappingException(I18N.format("unknown.persistent.class.property", persistentClass + '.' + propertyName));
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
        resources.addAll(mappings);
        for (Resource resource : mappings)
        {
            addInputStream(resource.getInputStream());
        }
    }

    public Configuration setProperties(Properties properties)
    {
        properties.putAll(properties);
        return super.setProperties(properties);
    }

    public Configuration setProperty(String string, String string1)
    {
        properties.setProperty(string, string1);

        return super.setProperty(string, string1);
    }

    public MutableConfiguration copy()
    {
        MutableConfiguration copy = new MutableConfiguration();
        copy.setProperties(properties);
        try
        {
            copy.addMappings(resources);
        }
        catch (IOException e)
        {
            // noop. any errors here would have happened on the original, so we ignore them.
        }

        for (Table tab : tabs)
        {
            copy.addTable(tab);
        }

        return copy;
    }

    public void setHibernateDialect(Properties jdbc)
    {
        String dialect = HibernateUtils.inferHibernateDialect(jdbc);
        setProperty(Environment.DIALECT, dialect);
    }
}

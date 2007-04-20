package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class Configuration
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private TypeRegistry typeRegistry;
    private ConfigurationRegistry configurationRegistry;

    private Record record;
    private Type type;

    private Type targetType;
    private String targetSymbolicName;

    private String path;
    private String[] pathElements;
    private String parentPath;
    private String[] parentPathElements;
    private String currentPath;

    private List<String> simpleProperties = new LinkedList<String>();
    private List<String> nestedProperties = new LinkedList<String>();
    private List<String> extensions = new LinkedList<String>();

    private boolean configurationCheckAvailable = false;

    public Configuration(String path)
    {
        if (!TextUtils.stringSet(path))
        {
            throw new IllegalArgumentException("Path must be provided.");
        }

        this.path = PathUtils.normalizePath(path);

        ComponentContext.autowire(this);
    }


    public void analyse()
    {
        // load the type defined by the path.
        pathElements = PathUtils.getPathElements(path);
        if (pathElements.length == 0)
        {
            return;
        }

        parentPathElements = PathUtils.getParentPathElements(pathElements);
        parentPath = PathUtils.getPath(parentPathElements);
        currentPath = pathElements[pathElements.length - 1];

        record = configurationPersistenceManager.getRecord(path);

        parentPath = PathUtils.getParentPath(path);

        type = configurationPersistenceManager.getType(path);
        targetType = type.getTargetType();

        if (targetType instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) targetType;
            targetSymbolicName = ctype.getSymbolicName();

            // FIXME if necessary
//            if (!ConfigurationExtension.class.isAssignableFrom(targetType.getClazz()))
//            {
                // only show a simple properties form if it is not associated with an extension type.
                for (String propertyName : ctype.getPropertyNames(PrimitiveType.class))
                {
                    simpleProperties.add(propertyName);
                }
                for (String propertyName : ctype.getPropertyNames(ReferenceType.class))
                {
                    simpleProperties.add(propertyName);
                }
//            }

            for (String propertyName : ctype.getPropertyNames(CompositeType.class))
            {
                nestedProperties.add(propertyName);
            }
            for (TypeProperty property: ctype.getProperties(CollectionType.class))
            {
                final CollectionType propertyType = (CollectionType) property.getType();
                if(!(propertyType.getCollectionType() instanceof SimpleType))
                {
                    nestedProperties.add(property.getName());
                }
            }

            extensions.addAll(((CompositeType) targetType).getExtensions());

            configurationCheckAvailable = configurationRegistry.getConfigurationCheckType(ctype) != null;
        }
    }

    public boolean isConfigurationCheckAvailable()
    {
        return configurationCheckAvailable;
    }

    public String getPath()
    {
        return path;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getCurrentPath()
    {
        return currentPath;
    }

    public String[] getParentPathElements()
    {
        return parentPathElements;
    }

    public String[] getPathElements()
    {
        return pathElements;
    }

    public Record getRecord()
    {
        return record;
    }

    public Type getType()
    {
        return type;
    }

    public Type getTargetType()
    {
        return targetType;
    }

    public List<String> getSimpleProperties()
    {
        return simpleProperties;
    }

    public List<String> getNestedProperties()
    {
        return nestedProperties;
    }

    public List<String> getExtensions()
    {
        return extensions;
    }

    public String getTargetSymbolicName()
    {
        return targetSymbolicName;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}

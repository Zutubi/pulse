package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 *
 */
public class Configuration
{
    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    private Record record;
    private Type type;
    private String typeSymbolicName;

    private Type targetType;
    private String targetSymbolicName;

    private String path;
    private List<String> pathElements;
    private String parentPath;
    private List<String> parentPathElements;
    private String currentPath;
    private List<String> simpleProperties = new LinkedList<String>();
    private List<String> nestedProperties = new LinkedList<String>();
    private List<String> listProperties = new LinkedList<String>();
    private List<String> mapProperties = new LinkedList<String>();
    private List<String> extensions = new LinkedList<String>();

    public Configuration(String path)
    {
        if (!TextUtils.stringSet(path))
        {
            throw new IllegalArgumentException("Path must be provided.");
        }

        this.path = normalizePath(path);

        ComponentContext.autowire(this);
    }

    private String normalizePath(String path)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        if (path.endsWith("/"))
        {
            path = path.substring(0, path.length() -1);
        }
        return path;
    }

    public void analyse()
    {
        // load the type defined by the path.
        pathElements = new LinkedList<String>();
        StringTokenizer tokens = new StringTokenizer(path, "/", false);
        while (tokens.hasMoreTokens())
        {
            pathElements.add(tokens.nextToken());
        }
        if (pathElements.size() == 0)
        {
            return;
        }
        
        parentPathElements = pathElements.subList(0, pathElements.size() - 1);

        parentPath = StringUtils.join("/", parentPathElements);
        currentPath = StringUtils.join("", pathElements.subList(pathElements.size() - 1, pathElements.size()));

        record = recordManager.load(path);

        parentPath = configurationPersistenceManager.getParentPath(path);

        type = configurationPersistenceManager.getType(path);

        typeSymbolicName = type.getSymbolicName();

        targetType = type;
        if (type instanceof CollectionType)
        {
            targetType = ((CollectionType)type).getCollectionType();
        }

        targetSymbolicName = targetType.getSymbolicName();
        
        for (String propertyName : targetType.getPropertyNames(PrimitiveType.class))
        {
            simpleProperties.add(propertyName);
        }
        for (String propertyName : targetType.getPropertyNames(CompositeType.class))
        {
            nestedProperties.add(propertyName);
        }
        for (String propertyName : targetType.getPropertyNames(ListType.class))
        {
            listProperties.add(propertyName);
        }
        for (String propertyName : targetType.getPropertyNames(MapType.class))
        {
            mapProperties.add(propertyName);
        }
        if (targetType instanceof CompositeType)
        {
            extensions.addAll(((CompositeType)targetType).getExtensions());
        }
    }

    public String getTypeSymbolicName()
    {
        return typeSymbolicName;
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

    public List<String> getParentPathElements()
    {
        return parentPathElements;
    }

    public List<String> getPathElements()
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

    public List<String> getListProperties()
    {
        return listProperties;
    }

    public List<String> getMapProperties()
    {
        return mapProperties;
    }

    public List<String> getExtensions()
    {
        return extensions;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public String getTargetSymbolicName()
    {
        return targetSymbolicName;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}

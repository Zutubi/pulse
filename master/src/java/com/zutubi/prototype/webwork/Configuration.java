package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.Traversable;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.util.StringUtils;
import com.opensymphony.util.TextUtils;

import java.util.Arrays;
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
    private TypeRegistry typeRegistry;
    private ConfigurationRegistry configurationRegistry;

    private Record record;
    private Type instanceType;
    private String instanceTypeSymbolicName;
    private Type type;
    private String typeSymbolicName;

    private Type collectionType;
    private String collectionSymbolicName;

    private Type targetType;
    private String targetSymbolicName;

    private Record parentRecord;
    private Type parentType;
    private String parentTypeSymbolicName;

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


        // if we have a record, is it a specific instance, and if so, analyse it.
        record = recordManager.load(path);
        if (record != null)
        {
            instanceTypeSymbolicName = record.getSymbolicName();
            if (instanceTypeSymbolicName != null)
            {
                instanceType = typeRegistry.getType(instanceTypeSymbolicName);

                // we have a record, and we have all of the type information that we need.
            }
            // else, it could be list/map data.
        }

        // analyse the parent...
        parentRecord = recordManager.load(parentPath);
        parentType = configurationRegistry.getType(parentPath);

        if (parentRecord != null)
        {
            String symbolicName = parentRecord.getSymbolicName();
            if (symbolicName != null)
            {
                parentType = typeRegistry.getType(symbolicName);
            }
        }

        if (parentType instanceof CompositeType)
        {
            parentTypeSymbolicName = ((CompositeType)parentType).getSymbolicName();
        }

        // using the parent context, we can navigate to the current paths type.
        if (parentType != null)
        {
            type = ((Traversable)parentType).getType(Arrays.asList(currentPath));
        }
        else
        {
            type = configurationRegistry.getType(path);
        }
        
        if (type instanceof CompositeType)
        {
            typeSymbolicName = ((CompositeType)type).getSymbolicName();
        }
        if (type instanceof CollectionType)
        {
            collectionType = ((CollectionType)type).getCollectionType();
            if (collectionType instanceof CompositeType)
            {
                collectionSymbolicName = ((CompositeType)collectionType).getSymbolicName();
            }
        }

        // analysis of the current type.
        targetType = type;
        if (instanceType != null)
        {
            targetType = instanceType;
        }
        if (collectionType != null)
        {
            targetType = collectionType;
        }
        
        if (targetType instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) targetType;
            targetSymbolicName = compositeType.getSymbolicName();
            
            for (String propertyName : compositeType.getProperties(PrimitiveType.class))
            {
                simpleProperties.add(propertyName);
            }
            for (String propertyName : compositeType.getProperties(CompositeType.class))
            {
                nestedProperties.add(propertyName);
            }
            for (String propertyName : compositeType.getProperties(ListType.class))
            {
                listProperties.add(propertyName);
            }
            for (String propertyName : compositeType.getProperties(MapType.class))
            {
                mapProperties.add(propertyName);
            }
            extensions.addAll(compositeType.getExtensions());
        }
    }

    public String getInstanceTypeSymbolicName()
    {
        return instanceTypeSymbolicName;
    }

    public String getTypeSymbolicName()
    {
        return typeSymbolicName;
    }

    public String getParentTypeSymbolicName()
    {
        return parentTypeSymbolicName;
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

    public Type getInstanceType()
    {
        return instanceType;
    }

    public Type getType()
    {
        return type;
    }

    public Type getCollectionType()
    {
        return collectionType;
    }

    public Type getTargetType()
    {
        return targetType;
    }

    public Record getParentRecord()
    {
        return parentRecord;
    }

    public Type getParentType()
    {
        return parentType;
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

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public String getCollectionSymbolicName()
    {
        return collectionSymbolicName;
    }

    public String getTargetSymbolicName()
    {
        return targetSymbolicName;
    }
}

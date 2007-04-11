package com.zutubi.prototype.type;

import com.zutubi.prototype.annotation.ID;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.util.AnnotationUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    private static final Logger LOG = Logger.getLogger(MapType.class);

    private ConfigurationPersistenceManager configurationPersistenceManager;
    private String keyProperty;

    public MapType(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        super(HashMap.class);
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public Map instantiate(String path, Object data) throws TypeException
    {
        Map instance = (Map) (path == null ? null : configurationPersistenceManager.getInstance(path));
        if (instance == null && data != null)
        {
            if (!(data instanceof Record))
            {
                throw new TypeConversionException("Expected a record, instead received " + data.getClass());
            }

            Record record = (Record) data;

            Type defaultType = getCollectionType();
            instance = new HashMap<String, Object>();
            if (path != null)
            {
                configurationPersistenceManager.putInstance(path, instance);
            }

            for (String key : record.keySet())
            {
                Object child = record.get(key);
                Type type = defaultType;
                if (child instanceof Record)
                {
                    Record childRecord = (Record) child;
                    type = typeRegistry.getType(childRecord.getSymbolicName());
                    if(type == null)
                    {
                        throw new TypeException("Reference to unrecognised type '" + childRecord.getSymbolicName() + "'");
                    }
                }

                Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, key), child);
                //noinspection unchecked
                instance.put(key, value);
            }
        }

        return instance;
    }

    public void setCollectionType(Type collectionType) throws TypeException
    {
        super.setCollectionType(collectionType);

        if(!(collectionType instanceof CompositeType))
        {
            throw new TypeException("Maps may only contain composite types");
        }

        CompositeType compositeType = (CompositeType) collectionType;

        // Unfortunately we cannot use the type registry information as we
        // are part way through registration and cyclical type structures
        // mean that the ID property may not yet have been found.
        Class clazz = compositeType.getClazz();
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for(PropertyDescriptor descriptor: beanInfo.getPropertyDescriptors())
            {
                List<Annotation> annotations = AnnotationUtils.annotationsFromProperty(descriptor);
                for(Annotation a: annotations)
                {
                    if(a.annotationType() == ID.class)
                    {
                        keyProperty = descriptor.getName();
                        break;
                    }
                }

                if(keyProperty != null)
                {
                    break;
                }
            }
        }
        catch (IntrospectionException e)
        {
            LOG.severe(e);
        }

        if(keyProperty == null)
        {
            throw new TypeException("Types stored in maps must have an @ID property");
        }
    }

    protected String getItemKey(String path, Record collectionRecord, Record itemRecord, RecordManager recordManager)
    {
        return (String) itemRecord.get(keyProperty);
    }

    public String save(String path, String baseName, Record record, RecordManager recordManager)
    {
        // Check for renames
        String newName = (String) record.get(keyProperty);
        if(baseName != null && !baseName.equals(newName))
        {
            // We need to update our own record and tell the CPM to update
            // references.
            String oldPath = PathUtils.getPath(path, baseName);
            String newPath = PathUtils.getPath(path, newName);

            // Rename references first, as the below changes to the record
            // will invalidate the reference index.
            configurationPersistenceManager.renameReferences(oldPath, newPath);

            recordManager.copy(oldPath, newPath);
            recordManager.delete(oldPath);
            recordManager.update(newPath, record);
            return newPath;
        }
        else
        {
            // Regular update.
            String newPath = PathUtils.getPath(path, baseName);
            recordManager.update(newPath, record);
            return newPath;
        }
    }
}

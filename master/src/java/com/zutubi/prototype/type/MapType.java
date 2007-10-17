package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.ConfigurationMap;
import com.zutubi.util.AnnotationUtils;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.beans.IntrospectionException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    private static final Logger LOG = Logger.getLogger(MapType.class);

    private String keyProperty;

    public MapType()
    {
        super(HashMap.class);
    }

    @SuppressWarnings({"unchecked"})
    public Map instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        Map instance = null;
        if (data != null)
        {
            if (!(data instanceof Record))
            {
                throw new TypeConversionException("Expected a record, instead received " + data.getClass());
            }

            instance = new ConfigurationMap();
        }

        return instance;
    }

    @SuppressWarnings({ "unchecked" })
    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        ConfigurationMap<Configuration> map = (ConfigurationMap<Configuration>) instance;
        covertFromRecord((Record) data, map, new InstantiateFromRecord(map, instantiator));
    }

    private void covertFromRecord(Record record, Map result, FromRecord fromRecord)
    {
        Type defaultType = getCollectionType();
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
                    fromRecord.handleFieldError(key, "Reference to unrecognised type '" + childRecord.getSymbolicName() + "'");
                    continue;
                }
            }

            try
            {
                result.put(key, fromRecord.convert(key, type, child));
            }
            catch (TypeException e)
            {
                fromRecord.handleFieldError(key, e.getMessage());
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public Object unstantiate(Object instance) throws TypeException
    {
        typeCheck(instance, Map.class);
        return convertToRecord((Map) instance, new UnstantiateToRecord());
    }

    public Object toXmlRpc(Object data) throws TypeException
    {
        if(data == null)
        {
            return null;
        }
        else
        {
            Record record = (Record) data;
            Hashtable<String, Object> result = new Hashtable<String, Object>(record.size());
            covertFromRecord(record, result, new XmlRpcFromRecord());
            return result;
        }
    }

    public Object fromXmlRpc(Object data) throws TypeException
    {
        typeCheck(data, Hashtable.class);
        return convertToRecord((Hashtable) data, new XmlRpcToRecord());
    }

    private MutableRecord convertToRecord(Map instance, ToRecord toRecord) throws TypeException
    {
        MutableRecord result = createNewRecord(true);
        Type collectionType = getCollectionType();
        for(Object entry: instance.entrySet())
        {
            Map.Entry e = (Map.Entry) entry;
            try
            {
                typeCheck(e.getKey(), String.class);
            }
            catch (TypeException ex)
            {
                throw new TypeException("Map element has invalid key type: " + ex.getMessage(), ex);
            }

            try
            {
                result.put((String) e.getKey(), toRecord.convert(collectionType, e.getValue()));
            }
            catch (TypeException ex)
            {
                throw new TypeException("Converting map element '" + e.getKey() + "': " + ex.getMessage(), ex);
            }
        }

        return result;
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
        try
        {
            keyProperty = AnnotationUtils.getPropertyAnnotatedWith(compositeType.getClazz(), ID.class);
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

    protected Comparator<String> getKeyComparator()
    {
        // Lexicographical ordering by the key.
        return new Sort.StringComparator();
    }

    public String getKeyProperty()
    {
        return keyProperty;
    }

    public String getInsertionPath(String path, Record record)
    {
        return PathUtils.getPath(path, (String) record.get(keyProperty));
    }

    public boolean isValid(Object instance)
    {
        Map map = (Map) instance;

        if(map instanceof ConfigurationMap && !((ConfigurationMap)map).isValid())
        {
            return false;
        }

        CompositeType collectionType = (CompositeType) getCollectionType();
        for(Object o: map.values())
        {
            if(!collectionType.isValid(o))
            {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({ "unchecked" })
    public void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException
    {
        f.process(instance);
        CompositeType collectionType = (CompositeType) getCollectionType();
        Map<String, Object> map = ((Map<String, Object>) instance);
        for(Map.Entry<String, Object> entry: map.entrySet())
        {
            f.push(entry.getKey());
            collectionType.forEachComplex(entry.getValue(), f);
            f.pop();
        }
    }

    public String getSavePath(String path, Record record)
    {
        String name = (String) record.get(keyProperty);
        if(name == null)
        {
            throw new IllegalArgumentException("Record has no " + keyProperty);
        }
        return PathUtils.getPath(PathUtils.getParentPath(path), name);
    }

    private static interface FromRecord
    {
        void handleFieldError(String key, String message);
        Object convert(String key, Type type, Object child) throws TypeException;
    }

    private static class InstantiateFromRecord implements FromRecord
    {
        private ConfigurationMap<? extends Configuration> map;
        private Instantiator instantiator;

        public InstantiateFromRecord(ConfigurationMap<? extends Configuration> map, Instantiator instantiator)
        {
            this.map = map;
            this.instantiator = instantiator;
        }

        public void handleFieldError(String key, String message)
        {
            map.addFieldError(key, message);
        }

        public Object convert(String key, Type type, Object child) throws TypeException
        {
            return instantiator.instantiate(key, true, type, child);
        }
    }

    private static class XmlRpcFromRecord implements FromRecord
    {
        public void handleFieldError(String key, String message)
        {
            // Do nothing.
        }

        public Object convert(String key, Type type, Object child) throws TypeException
        {
            return type.toXmlRpc(child);
        }
    }

    private static interface ToRecord
    {
        Object convert(Type type, Object object) throws TypeException;
    }

    private static class UnstantiateToRecord implements ToRecord
    {
        public Object convert(Type type, Object object) throws TypeException
        {
            return type.unstantiate(object);
        }
    }

    private static class XmlRpcToRecord implements ToRecord
    {
        public Object convert(Type type, Object object) throws TypeException
        {
            return type.fromXmlRpc(object);
        }
    }
}

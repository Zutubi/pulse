package com.zutubi.prototype.type;

import com.zutubi.config.annotations.ID;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.ConfigurationMap;
import com.zutubi.util.AnnotationUtils;
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
        addItems((Record) data, new InstantiateAdder((ConfigurationMap<String, Object>) instance, instantiator));
    }

    private void addItems(Record record, Adder adder)
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
                    adder.handleFieldError(key, "Reference to unrecognised type '" + childRecord.getSymbolicName() + "'");
                    continue;
                }
            }

            try
            {
                adder.add(key, type, child);
            }
            catch (TypeException e)
            {
                adder.handleFieldError(key, e.getMessage());
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public Object unstantiate(Object instance) throws TypeException
    {
        if(!(instance instanceof Map))
        {
            throw new TypeException("Expecting map, got '" + instance.getClass().getName() + "'");
        }

        MutableRecord result = createNewRecord(true);
        Map<String, Object> map = (Map<String, Object>) instance;
        Type collectionType = getCollectionType();
        for(Map.Entry<String, Object> entry: map.entrySet())
        {
            result.put(entry.getKey(), collectionType.unstantiate(entry.getValue()));
        }

        return result;
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
            addItems(record, new XmlRpcAdder(result));
            return result;
        }
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

    public String getSavePath(String path, Record record)
    {
        return PathUtils.getPath(PathUtils.getParentPath(path), (String) record.get(keyProperty));
    }

    private static interface Adder
    {
        void handleFieldError(String key, String message);
        void add(String key, Type type, Object child) throws TypeException;
    }

    private static class InstantiateAdder implements Adder
    {
        private ConfigurationMap<String, Object> map;
        private Instantiator instantiator;

        public InstantiateAdder(ConfigurationMap<String, Object> map, Instantiator instantiator)
        {
            this.map = map;
            this.instantiator = instantiator;
        }

        public void handleFieldError(String key, String message)
        {
            map.addFieldError(key, message);
        }

        public void add(String key, Type type, Object child) throws TypeException
        {
            map.put(key, instantiator.instantiate(key, true, type, child));
        }
    }

    private static class XmlRpcAdder implements Adder
    {
        private Hashtable<String, Object> hashtable;

        public XmlRpcAdder(Hashtable<String, Object> hashtable)
        {
            this.hashtable = hashtable;
        }

        public void handleFieldError(String key, String message)
        {
            // Do nothing.
        }

        public void add(String key, Type type, Object child) throws TypeException
        {
            hashtable.put(key, type.toXmlRpc(child));
        }
    }
}

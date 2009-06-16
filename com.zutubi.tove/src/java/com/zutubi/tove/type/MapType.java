package com.zutubi.tove.type;

import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.config.ConfigurationMap;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.AnnotationUtils;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.beans.IntrospectionException;
import java.util.*;

/**
 *
 *
 */
public class MapType extends CollectionType
{
    private static final Logger LOG = Logger.getLogger(MapType.class);

    private String keyProperty;

    public MapType(Type collectionType, TypeRegistry typeRegistry) throws TypeException
    {
        super(HashMap.class, collectionType, typeRegistry);
    }

    protected void verifyCollectionType(Type collectionType) throws TypeException
    {
        super.verifyCollectionType(collectionType);

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

    public CompositeType getTargetType()
    {
        return (CompositeType) super.getTargetType();
    }

    public boolean deepValueEquals(Object data1, Object data2)
    {
        if(!(data1 instanceof Record))
        {
            throw new IllegalArgumentException("Expecting record, got '" + data1.getClass().getName() + "'");
        }

        if(!(data2 instanceof Record))
        {
            throw new IllegalArgumentException("Expecting record, got '" + data2.getClass().getName() + "'");
        }

        Record r1 = (Record) data1;
        Record r2 = (Record) data2;

        // Compare nested values using their type
        List<String> nested1 = getOrder(r1);
        List<String> nested2 = getOrder(r2);
        if(!nested1.equals(nested2))
        {
            return false;
        }

        Type collectionType = getCollectionType();
        for (String key: nested1)
        {
            if(!collectionType.deepValueEquals(r1.get(key), r2.get(key)))
            {
                return false;
            }
        }

        return true;
    }

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

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        @SuppressWarnings({ "unchecked" })
        ConfigurationMap<Configuration> map = (ConfigurationMap<Configuration>) instance;
        covertFromRecord((Record) data, map, new InstantiateFromRecord(map, instantiator));
    }

    @SuppressWarnings({ "unchecked" })
    private void covertFromRecord(Record record, Map result, FromRecord fromRecord)
    {
        Type defaultType = getCollectionType();
        for (String key : getOrder(record))
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

    public MutableRecord unstantiate(Object instance) throws TypeException
    {
        if (instance == null)
        {
            return null;
        }

        typeCheck(instance, Map.class);
        return convertToRecord((Map) instance, new UnstantiateToRecord());
    }

    public Object toXmlRpc(String templateOwnerPath, Object data) throws TypeException
    {
        if(data == null)
        {
            return null;
        }
        else
        {
            Record record = (Record) data;
            Hashtable<String, Object> result = new Hashtable<String, Object>(record.size());
            covertFromRecord(record, result, new XmlRpcFromRecord(templateOwnerPath));
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
        copyMetaToRecord(instance, result);

        List<String> order = new LinkedList<String>();
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

            order.add((String) e.getKey());

            try
            {
                result.put((String) e.getKey(), toRecord.convert(collectionType, e.getValue()));
            }
            catch (TypeException ex)
            {
                throw new TypeException("Converting map element '" + e.getKey() + "': " + ex.getMessage(), ex);
            }
        }

        if(isOrdered() && order.size() > 0)
        {
            // Only set an explicit order when it is different from the
            // default.  This prevents instance saves from unnecessarily
            // adding an explicit order to a collection.
            List<String> defaultOrder = getOrder(result);
            if(!order.equals(defaultOrder))
            {
                setOrder(result, order);
            }            
        }
        return result;
    }

    public Comparator<String> getKeyComparator(final Record record)
    {
        if(isOrdered())
        {
            // Insertion order: determined by the handle
            return new Comparator<String>()
            {
                public int compare(String o1, String o2)
                {
                    Record r1 = (Record) record.get(o1);
                    Record r2 = (Record) record.get(o2);

                    if(r1 == null || r2 == null)
                    {
                        return 0;
                    }
                    else
                    {
                        return (int) (r1.getHandle() - r2.getHandle());
                    }
                }
            };
        }
        else
        {
            // Lexicographical ordering by the key.
            return new Sort.StringComparator();
        }
    }

    public String getKeyProperty()
    {
        return keyProperty;
    }

    public String getItemKey(String path, Record record)
    {
        String key = (String) record.get(keyProperty);
        if (key == null)
        {
            throw new IllegalArgumentException("Record has no " + keyProperty);
        }
        return key;
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

    public void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException
    {
        f.process(instance);
        CompositeType collectionType = (CompositeType) getCollectionType();
        @SuppressWarnings({ "unchecked" })
        Map<String, Object> map = ((Map<String, Object>) instance);
        for(Map.Entry<String, Object> entry: map.entrySet())
        {
            f.push(entry.getKey());
            collectionType.forEachComplex(entry.getValue(), f);
            f.pop();
        }
    }

    public boolean hasSignificantKeys()
    {
        return true;
    }

    public String toString()
    {
        return "map[" + getCollectionType().toString() + "]";
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
        private String templateOwnerPath;

        public XmlRpcFromRecord(String templateOwnerPath)
        {
            this.templateOwnerPath = templateOwnerPath;
        }

        public void handleFieldError(String key, String message)
        {
            // Do nothing.
        }

        public Object convert(String key, Type type, Object child) throws TypeException
        {
            return type.toXmlRpc(templateOwnerPath, child);
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

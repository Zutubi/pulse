package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.ConfigurationSet;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.Sort;

import java.util.Comparator;
import java.util.Set;

/**
 */
public class SetType extends CollectionType
{
    public SetType(Class type, Type collectionType, TypeRegistry typeRegistry) throws TypeException
    {
        super(type, collectionType, typeRegistry);
    }

    protected void verifyCollectionType(Type collectionType) throws TypeException
    {
        super.verifyCollectionType(collectionType);
        if(!(collectionType instanceof CompositeType))
        {
            throw new TypeException("Sets may only contain composite types");
        }
    }

    public Comparator<String> getKeyComparator(Record record)
    {
        return new Sort.StringComparator();
    }

    public Object instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        Set instance = null;
        if (data != null)
        {
            if (!(data instanceof Record))
            {
                throw new TypeConversionException("Expected a record, instead received " + data.getClass());
            }

            instance = new ConfigurationSet();
        }

        return instance;
    }

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        ConfigurationSet<Configuration> set = (ConfigurationSet<Configuration>) instance;
        covertFromRecord((Record) data, set, new InstantiateFromRecord(set, instantiator));
    }

    private void covertFromRecord(Record record, Set result, FromRecord fromRecord)
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
                result.add(fromRecord.convert(key, type, child));
            }
            catch (TypeException e)
            {
                fromRecord.handleFieldError(key, e.getMessage());
            }
        }
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        typeCheck(instance, Set.class);
        return convertToRecord((Set) instance, new UnstantiateToRecord());
    }

    private MutableRecord convertToRecord(Set instance, ToRecord toRecord) throws TypeException
    {
        MutableRecord result = createNewRecord(true);
        copyMetaToRecord(instance, result);

        Type collectionType = getCollectionType();
        for(Object item: instance)
        {
            try
            {
                Record record = toRecord.convert(collectionType, item);
                result.put(getItemKey(record), record);
            }
            catch (TypeException ex)
            {
                throw new TypeException("Converting set element: " + ex.getMessage(), ex);
            }
        }

        return result;
    }

    private String getItemKey(Record item)
    {
        return item.toString();
    }

    public Object toXmlRpc(Object data) throws TypeException
    {
        throw new RuntimeException("Not implemented");
    }

    public Object fromXmlRpc(Object data) throws TypeException
    {
        throw new RuntimeException("Not implemented");
    }

    public String getSavePath(String path, Record record)
    {
        throw new RuntimeException("Not implemented");
    }

    public String getInsertionPath(String path, Record record)
    {
        throw new RuntimeException("Not implemented");
    }

    public boolean isValid(Object instance)
    {
        throw new RuntimeException("Not implemented");
    }

    public void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException
    {
        throw new RuntimeException("Not implemented");
    }

    // FIXME: share this with MapType??
    private static interface FromRecord
    {
        void handleFieldError(String key, String message);
        Object convert(String key, Type type, Object child) throws TypeException;
    }

    private static class InstantiateFromRecord implements FromRecord
    {
        private ConfigurationSet<? extends Configuration> set;
        private Instantiator instantiator;

        public InstantiateFromRecord(ConfigurationSet<? extends Configuration> set, Instantiator instantiator)
        {
            this.set = set;
            this.instantiator = instantiator;
        }

        public void handleFieldError(String key, String message)
        {
            set.addFieldError(key, message);
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
        Record convert(Type type, Object object) throws TypeException;
    }

    private static class UnstantiateToRecord implements ToRecord
    {
        public Record convert(Type type, Object object) throws TypeException
        {
            return (Record) type.unstantiate(object);
        }
    }

    private static class XmlRpcToRecord implements ToRecord
    {
        public Record convert(Type type, Object object) throws TypeException
        {
            return (Record) type.fromXmlRpc(object);
        }
    }
}

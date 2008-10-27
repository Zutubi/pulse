package com.zutubi.tove.type;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.ConfigurationList;
import com.zutubi.tove.type.record.HandleAllocator;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.GraphFunction;

import java.util.*;

/**
 *
 *
 */
public class ListType extends CollectionType
{
    private HandleAllocator handleAllocator;

    public ListType(HandleAllocator handleAllocator, Type collectionType, TypeRegistry typeRegistry) throws TypeException
    {
        super(LinkedList.class, collectionType, typeRegistry);
        this.handleAllocator = handleAllocator;
    }

    public boolean deepValueEquals(Object data1, Object data2)
    {
        if(data1 instanceof Record)
        {
            if(!(data2 instanceof Record))
            {
                return false;
            }

            Record r1 = (Record) data1;
            Record r2 = (Record) data2;

            List<String> order1 = getOrder(r1);
            List<String> order2 = getOrder(r2);

            // Keys are not significant in lists, so we don't compare the key
            // values.
            if(order1.size() != order2.size())
            {
                return false;
            }

            Type collectionType = getCollectionType();
            for (int i = 0; i < order1.size(); i++)
            {
                if(!collectionType.deepValueEquals(r1.get(order1.get(i)), r2.get(order2.get(i))))
                {
                    return false;
                }
            }

            return true;
        }
        else if(data1 instanceof String[])
        {
            if(!(data2 instanceof String[]))
            {
                return false;
            }

            return Arrays.equals((String[]) data1, (String[]) data2);
        }

        throw new IllegalArgumentException("Expecting record or string array, got '" + data1.getClass().getName() + "'");
    }

    public List<Object> instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        if(data == null)
        {
            return null;
        }
        else if(data instanceof Record)
        {
            return new ConfigurationList<Object>();
        }
        else if(data instanceof String[])
        {
            List<Object> list = new LinkedList<Object>();
            Type type = getCollectionType();
            String[] references = (String[]) data;
            for (int i = 0; i < references.length; i++)
            {
                list.add(instantiator.instantiate(Integer.toString(i), true, type, references[i]));
            }

            return list;
        }
        else
        {
            throw new TypeConversionException("Expected a record or string array, instead received " + data.getClass());
        }
    }

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        if (data instanceof Record)
        {
            @SuppressWarnings({ "unchecked" })
            ConfigurationList<Object> list = (ConfigurationList<Object>) instance;
            FromRecord adder = new InstantiateFromRecord(list, instantiator);
            convertFromRecord((Record) data, list, adder);
        }
    }

    private void convertFromRecord(Record record, Collection<Object> collection, FromRecord fromRecord)
    {
        Collection<String> keys = getOrder(record);
        Type defaultType = getCollectionType();
        for (String key : keys)
        {
            Object child = record.get(key);
            Type type = defaultType;
            if (child instanceof Record)
            {
                Record childRecord = (Record) child;
                String symbolicName = childRecord.getSymbolicName();
                type = typeRegistry.getType(symbolicName);
                if(type == null)
                {
                    fromRecord.handleFieldError(key, "Reference to unknown type '" + symbolicName + "'");
                    continue;
                }
            }

            try
            {
                collection.add(fromRecord.convert(key, type, child));
            }
            catch (TypeException e)
            {
                fromRecord.handleFieldError(key, e.getMessage());
            }
        }
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        typeCheck(instance, List.class);

        List list = (List) instance;
        Type collectionType = getCollectionType();
        if(collectionType instanceof SimpleType)
        {
            // Make it a String array
            String[] result = new String[list.size()];
            int i = 0;
            for(Object o: list)
            {
                result[i++] = (String) collectionType.unstantiate(o);
            }

            return result;
        }
        else
        {
            // A sub-record.  Need to set the ordering based on the iteration
            // order of the list.
            return convertToRecord(list, collectionType, new UnstantiateToRecord());
        }
    }

    private MutableRecord convertToRecord(Collection collection, Type collectionType, ToRecord toRecord) throws TypeException
    {
        MutableRecord result = createNewRecord(true);
        copyMetaToRecord(collection, result);

        List<String> order = new ArrayList<String>(collection.size());
        for(Object o: collection)
        {
            String key;
            if(o instanceof Configuration && ((Configuration)o).getConfigurationPath() != null)
            {
                key = PathUtils.getBaseName(((Configuration)o).getConfigurationPath());
            }
            else
            {
                key = Long.toString(handleAllocator.allocateHandle());
            }
            
            try
            {
                result.put(key, toRecord.convert(collectionType, o));
            }
            catch (TypeException e)
            {
                throw new TypeException("Converting list element: " + e.getMessage());
            }
            order.add(key);
        }

        if (isOrdered() && order.size() > 0)
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

    public Object toXmlRpc(Object data) throws TypeException
    {
        if(data == null)
        {
            return null;
        }
        else if(getCollectionType() instanceof SimpleType)
        {
            typeCheck(data, String[].class);

            String[] items = (String[]) data;
            Type type = getCollectionType();
            Vector<Object> result = new Vector<Object>(items.length);
            for(String item: items)
            {
                result.add(type.toXmlRpc(item));
            }

            return result;
        }
        else
        {
            typeCheck(data, Record.class);

            Record record = (Record) data;
            Vector<Object> result = new Vector<Object>(record.size());
            convertFromRecord(record, result, new XmlRpcFromRecord());
            return result;
        }
    }

    public Object fromXmlRpc(Object data) throws TypeException
    {
        typeCheck(data, Vector.class);
        Vector vector = (Vector) data;

        Type collectionType = getCollectionType();
        if(collectionType instanceof SimpleType)
        {
            SimpleType simpleType = (SimpleType) collectionType;

            String[] result = new String[vector.size()];
            int i = 0;
            for(Object item: vector)
            {
                try
                {
                    result[i++] = simpleType.fromXmlRpc(item);
                }
                catch (TypeException e)
                {
                    throw new TypeException("Converting list element: " + e.getMessage(), e);
                }
            }

            return result;
        }
        else
        {
            return convertToRecord(vector, collectionType, new XmlRpcToRecord());
        }
    }

    public String getItemKey(String path, Record record)
    {
        if(path == null)
        {
            return Long.toString(handleAllocator.allocateHandle());
        }
        else
        {
            return PathUtils.getBaseName(path);
        }
    }

    public boolean isValid(Object instance)
    {
        List list = (List) instance;
        if(list instanceof ConfigurationList && !((ConfigurationList)list).isValid())
        {
            return false;
        }

        if(getCollectionType() instanceof SimpleType)
        {
            return true;
        }
        else
        {
            ComplexType collectionType = (ComplexType) getCollectionType();
            for(Object element: list)
            {
                if(!collectionType.isValid(element))
                {
                    return false;
                }
            }

            return true;
        }
    }

    public void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException
    {
        f.process(instance);
        if(getCollectionType() instanceof ComplexType)
        {
            ComplexType collectionType = (ComplexType) getCollectionType();
            int i = 0;
            for(Object element: (List)instance)
            {
                f.push("[" + i + "]");
                collectionType.forEachComplex(element, f);
                f.pop();
                i++;
            }
        }
    }

    public boolean hasSignificantKeys()
    {
        return false;
    }

    public Comparator<String> getKeyComparator(Record record)
    {
        // We want the items to appear in their inserted order.  We rely on
        // the ever-increasing handles to allow this.
        return new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                try
                {
                    long l1 = Long.parseLong(o1);
                    long l2 = Long.parseLong(o2);

                    return (int) (l1 - l2);
                }
                catch (NumberFormatException e)
                {
                    return 0;
                }
            }
        };
    }

    public String toString()
    {
        return "list[" + getCollectionType().toString() + "]";
    }

    private static interface FromRecord
    {
        void handleFieldError(String key, String error);
        Object convert(String key, Type type, Object child) throws TypeException;
    }

    private static class InstantiateFromRecord implements FromRecord
    {
        private ConfigurationList<Object> list;
        private Instantiator instantiator;

        public InstantiateFromRecord(ConfigurationList<Object> list, Instantiator instantiator)
        {
            this.list = list;
            this.instantiator = instantiator;
        }

        public void handleFieldError(String key, String error)
        {
            list.addFieldError(key, error);
        }

        public Object convert(String key, Type type, Object child) throws TypeException
        {
            return instantiator.instantiate(key, true, type, child);
        }
    }

    private static class XmlRpcFromRecord implements FromRecord
    {
        public void handleFieldError(String key, String error)
        {
            // Do nothing
        }

        public Object convert(String key, Type type, Object child) throws TypeException
        {
            return type.toXmlRpc(child);
        }
    }

    private static interface ToRecord
    {
        Object convert(Type type, Object data) throws TypeException;
    }

    private static class UnstantiateToRecord implements ToRecord
    {
        public Object convert(Type type, Object data) throws TypeException
        {
            return type.unstantiate(data);
        }
    }

    private static class XmlRpcToRecord implements ToRecord
    {
        public Object convert(Type type, Object data) throws TypeException
        {
            return type.fromXmlRpc(data);
        }
    }
}


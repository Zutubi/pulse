package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.HandleAllocator;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.ConfigurationList;

import java.util.*;

/**
 *
 *
 */
public class ListType extends CollectionType
{
    private HandleAllocator handleAllocator;

    public ListType(HandleAllocator handleAllocator)
    {
        super(LinkedList.class);
        this.handleAllocator = handleAllocator;
    }

    @SuppressWarnings({"unchecked"})
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

    @SuppressWarnings({ "unchecked" })
    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        if (data instanceof Record)
        {
            ConfigurationList<Object> list = (ConfigurationList<Object>) instance;
            Adder adder = new InstantiateAdder(list, instantiator);
            addItems((Record) data, adder);
        }
    }

    private void addItems(Record record, Adder adder)
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
                    adder.handleFieldError(key, "Reference to unknown type '" + symbolicName + "'");
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

    public Object unstantiate(Object instance) throws TypeException
    {
        if(!(instance instanceof List))
        {
            throw new TypeException("Expecting list, got '" + instance.getClass().getName() + "'");
        }

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
            MutableRecord result = createNewRecord(true);
            List<String> order = new ArrayList<String>(list.size());
            for(Object o: list)
            {
                String key = Long.toString(handleAllocator.allocateHandle());
                result.put(key, collectionType.unstantiate(o));
                order.add(key);
            }

            if (isOrdered() && order.size() > 0)
            {
                setOrder(result, order);
            }
            
            return result;
        }
    }

    public Object toXmlRpc(Object data) throws TypeException
    {
        if(data == null)
        {
            return null;
        }
        else if(data instanceof Record)
        {
            Record record = (Record) data;
            Vector<Object> result = new Vector<Object>(record.size());
            addItems(record, new XmlRpcAdder(result));
            return result;
        }
        else
        {
            String[] items = (String[]) data;
            Type type = getCollectionType();
            Vector<Object> result = new Vector<Object>(items.length);
            for(String item: items)
            {
                result.add(type.toXmlRpc(item));
            }

            return result;
        }
    }

    public String getInsertionPath(String path, Record record)
    {
        return PathUtils.getPath(path, Long.toString(handleAllocator.allocateHandle()));
    }

    public String getSavePath(String path, Record record)
    {
        return path;
    }

    protected Comparator<String> getKeyComparator()
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

    private static interface Adder
    {
        void handleFieldError(String key, String error);
        void add(String key, Type type, Object child) throws TypeException;
    }

    private static class InstantiateAdder implements Adder
    {
        private ConfigurationList<Object> list;
        private Instantiator instantiator;

        public InstantiateAdder(ConfigurationList<Object> list, Instantiator instantiator)
        {
            this.list = list;
            this.instantiator = instantiator;
        }

        public void handleFieldError(String key, String error)
        {
            list.addFieldError(key, error);
        }

        public void add(String key, Type type, Object child) throws TypeException
        {
            list.add(instantiator.instantiate(key, true, type, child));
        }
    }

    private static class XmlRpcAdder implements Adder
    {
        private Vector<Object> vector;

        public XmlRpcAdder(Vector<Object> vector)
        {
            this.vector = vector;
        }

        public void handleFieldError(String key, String error)
        {
            // Do nothing
        }

        public void add(String key, Type type, Object child) throws TypeException
        {
            vector.add(type.toXmlRpc(child));
        }
    }
}

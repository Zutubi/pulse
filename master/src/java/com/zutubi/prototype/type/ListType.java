package com.zutubi.prototype.type;

import com.zutubi.prototype.config.InstanceCache;
import com.zutubi.prototype.type.record.HandleAllocator;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;

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

    public Object emptyInstance()
    {
        return new ArrayList(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<Object> instantiate(String path, InstanceCache cache, Object data) throws TypeException
    {
        List<Object> instance = (List<Object>) (path == null ? null : cache.get(path));
        if (instance == null && data != null)
        {
            if (data instanceof Record)
            {
                Record record = (Record) data;

                instance = create(path, cache);

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
                            throw new TypeException("Reference to unknown type '" + symbolicName + "'");
                        }
                    }
                    Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, key), cache, child);
                    instance.add(value);
                }

                return instance;
            }
            else if(data instanceof String[])
            {
                instance = create(path, cache);
                Type type = getCollectionType();
                String[] references = (String[]) data;
                for (String reference : references)
                {
                    instance.add(type.instantiate(path, cache, reference));
                }
                return instance;
            }
            else
            {
                throw new TypeConversionException("Expected a record or string array, instead received " + data.getClass());
            }
        }

        return instance;
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

    private List<Object> create(String path, InstanceCache cache)
    {
        List<Object> instance;
        instance = new LinkedList<Object>();
        if (path != null)
        {
            cache.put(path, instance);
        }
        return instance;
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
}

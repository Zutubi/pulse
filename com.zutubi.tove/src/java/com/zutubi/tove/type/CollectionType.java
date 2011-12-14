package com.zutubi.tove.type;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.WebUtils;

import java.util.*;

/**
 * Base for types that represent collections like maps and lists.
 */
public abstract class CollectionType extends AbstractType implements ComplexType
{
    public static final String ORDER_KEY  = "order";

    private static final char SEPARATOR = ',';
    private static final String[] ITEM_REFERENCING_KEYS = { ORDER_KEY, TemplateRecord.HIDDEN_KEY };

    private Type collectionType;
    private boolean ordered = false;

    public CollectionType(Class type, Type collectionType, TypeRegistry typeRegistry) throws TypeException
    {
        super(type);
        verifyCollectionType(collectionType);
        this.collectionType = collectionType;
        this.typeRegistry = typeRegistry;
    }

    protected void verifyCollectionType(Type collectionType) throws TypeException
    {
        if(collectionType instanceof CollectionType)
        {
            throw new TypeException("Collection cannot itself hold a collection type");
        }
    }

    public Type getTargetType()
    {
        return collectionType;
    }

    public Type getCollectionType()
    {
        return collectionType;
    }

    public static List<String> getDeclaredOrder(Record record)
    {
        String order = record.getMeta(ORDER_KEY);
        if(order == null || order.length() == 0)
        {
            return new LinkedList<String>();
        }

        return new LinkedList<String>(WebUtils.splitAndDecode(SEPARATOR, order));
    }

    public List<String> getOrder(Record record)
    {
        List<String> order = getDeclaredOrder(record);

        // Remove non-existent keys
        Set<String> keySet = record.keySet();
        Iterator<String> it = order.iterator();
        while (it.hasNext())
        {
            String key = it.next();

            // Filter out keys that do not exist (can validly happen with
            // hidden items and inherited orders).
            if(!keySet.contains(key))
            {
                it.remove();
            }
        }

        // Add remaining keys (i.e. those not represented in the order)
        List<String> remaining = new LinkedList<String>(keySet);
        remaining.removeAll(order);
        Collections.sort(remaining, getKeyComparator(record));
        order.addAll(remaining);

        return order;
    }

    public static void setOrder(MutableRecord record, Collection<String> order)
    {
        record.putMeta(ORDER_KEY, WebUtils.encodeAndJoin(SEPARATOR, order));
    }

    public abstract String getItemKey(String path, Record record);
    public abstract Comparator<String> getKeyComparator(Record record);
    public abstract Collection<?> getItems(Configuration instance);

    public MutableRecord createNewRecord(boolean applyDefaults)
    {
        return new MutableRecordImpl();
    }

    public boolean isOrdered()
    {
        return ordered;
    }

    public void setOrdered(boolean ordered)
    {
        this.ordered = ordered;
    }

    public boolean isTemplated()
    {
        return false;
    }

    public Type getDeclaredPropertyType(String propertyName)
    {
        return collectionType;
    }

    public Type getActualPropertyType(String propertyName, Object propertyValue)
    {
        return collectionType.getActualType(propertyValue);
    }

    public boolean updateKeyReferences(MutableRecord record, String oldKey, String newKey)
    {
        boolean changes = false;
        for(String metaKey: ITEM_REFERENCING_KEYS)
        {
            String value = record.getMeta(metaKey);
            if(value != null)
            {
                List<String> keys = WebUtils.splitAndDecode(SEPARATOR, value);
                if(Collections.replaceAll(keys, oldKey, newKey))
                {
                    record.putMeta(metaKey, WebUtils.encodeAndJoin(SEPARATOR, keys));
                    changes = true;
                }
            }
        }

        return changes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CollectionType that = (CollectionType) o;

        if (ordered != that.ordered)
        {
            return false;
        }
        if (collectionType != null ? !collectionType.equals(that.collectionType) : that.collectionType != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = collectionType != null ? collectionType.hashCode() : 0;
        result = 31 * result + (ordered ? 1 : 0);
        return result;
    }
}

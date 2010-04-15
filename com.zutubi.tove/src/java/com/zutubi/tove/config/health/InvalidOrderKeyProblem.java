package com.zutubi.tove.config.health;

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.NotEqualsPredicate;

import java.util.List;

/**
 * Marks a key in a collection order that does not reference a valid item.
 */
public class InvalidOrderKeyProblem extends HealthProblemSupport
{
    private String key;

    /**
     * Creates a new problem for the given invalid key in the order of the
     * record at the given path.
     * 
     * @param path    path of the record the order is defined in
     * @param message message describing this problem
     * @param key     invalid key found
     */
    protected InvalidOrderKeyProblem(String path, String message, String key)
    {
        super(path, message);
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        Record record = recordManager.select(getPath());
        if (record != null && !(record.get(key) instanceof Record))
        {
            List<String> orderKeys = CollectionType.getDeclaredOrder(record);
            if (orderKeys.contains(key))
            {
                MutableRecord mutable = record.copy(false, true);
                CollectionType.setOrder(mutable, CollectionUtils.filter(orderKeys, new NotEqualsPredicate<String>(key)));
                recordManager.update(getPath(), mutable);
            }
        }
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
        if (!super.equals(o))
        {
            return false;
        }

        InvalidOrderKeyProblem that = (InvalidOrderKeyProblem) o;

        if (key != null ? !key.equals(that.key) : that.key != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
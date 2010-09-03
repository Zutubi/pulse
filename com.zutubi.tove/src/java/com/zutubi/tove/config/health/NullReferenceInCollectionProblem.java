package com.zutubi.tove.config.health;

import com.zutubi.tove.type.ReferenceType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.NotEqualsPredicate;

import java.util.Arrays;

/**
 * Identifies a null reference that is a member of a collection.
 */
public class NullReferenceInCollectionProblem extends HealthProblemSupport
{
    private String key;

    /**
     * Creates a new problem for the given invalid reference at the given key
     * of the record at the given path.
     *
     * @param path    path of the record the reference is defined in
     * @param message message describing this problem
     * @param key     the property where the reference was found
     */
    protected NullReferenceInCollectionProblem(String path, String message, String key)
    {
        super(path, message);
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        // Filter out all occurences of the null reference from the collection.
        Record record = recordManager.select(getPath());
        if (record != null)
        {
            Object value = record.get(key);
            if (value != null)
            {
                if (value instanceof String[])
                {
                    String[] references = (String[]) value;
                    String[] filteredReferences = CollectionUtils.filterToArray(references, new NotEqualsPredicate<String>(ReferenceType.NULL_REFERENCE));
                    if (!Arrays.equals(references, filteredReferences))
                    {
                        MutableRecord mutableRecord = record.copy(false, true);
                        mutableRecord.put(key, filteredReferences);
                        recordManager.update(getPath(), mutableRecord);
                    }
                }
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

        NullReferenceInCollectionProblem that = (NullReferenceInCollectionProblem) o;

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
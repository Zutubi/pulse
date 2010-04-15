package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.NotEqualsPredicate;

import java.util.Arrays;

/**
 * Marks an invalid reference, which may be singular or part of a reference
 * list.
 */
public class InvalidReferenceProblem extends HealthProblemSupport
{
    private String key;
    private String referencedHandle;

    /**
     * Creates a new problem for the given invalid reference at the given key
     * of the record at the given path.
     * 
     * @param path             path of the record the reference is defined in
     * @param message          message describing this problem
     * @param key              the property where the reference was found
     * @param referencedHandle the invalid reference value
     */
    protected InvalidReferenceProblem(String path, String message, String key, String referencedHandle)
    {
        super(path, message);
        this.key = key;
        this.referencedHandle = referencedHandle;
    }

    public void solve(RecordManager recordManager)
    {
        Record record = recordManager.select(getPath());
        if (record != null)
        {
            Object value = record.get(key);
            if (value != null)
            {
                if (value instanceof String && value.equals(referencedHandle))
                {
                    MutableRecord mutableRecord = record.copy(false, true);
                    mutableRecord.put(key, "0");
                    recordManager.update(getPath(), mutableRecord);
                }
                else if (value instanceof String[])
                {
                    String[] references = (String[]) value;
                    String[] filteredReferences = CollectionUtils.filterToArray(references, new NotEqualsPredicate<String>(referencedHandle));
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

        InvalidReferenceProblem that = (InvalidReferenceProblem) o;

        if (key != null ? !key.equals(that.key) : that.key != null)
        {
            return false;
        }
        if (referencedHandle != null ? !referencedHandle.equals(that.referencedHandle) : that.referencedHandle != null)
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
        result = 31 * result + (referencedHandle != null ? referencedHandle.hashCode() : 0);
        return result;
    }
}
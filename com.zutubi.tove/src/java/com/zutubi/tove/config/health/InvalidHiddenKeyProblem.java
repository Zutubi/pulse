package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;

import java.util.Set;

/**
 * Identifies a hidden key that does not reference a valid item.  This could
 * be a bad string value (not convertable to an integer), a non-existant handle
 * etc.
 */
public class InvalidHiddenKeyProblem extends HealthProblemSupport
{
    private String key;

    /**
     * Creates a new problem for the given invalid key in the hidden keys of
     * the record at the given path.
     * 
     * @param path    path of the record the hidden keys are defined in
     * @param message message describing this problem
     * @param key     invalid key found
     */
    protected InvalidHiddenKeyProblem(String path, String message, String key)
    {
        super(path, message);
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        // If the record still exists with the bad hidden key, remove all
        // occurrences of that key and update.  
        Record record = recordManager.select(getPath());
        if (record != null)
        {
            Set<String> hiddenKeys = TemplateRecord.getHiddenKeys(record);
            if (hiddenKeys.contains(key))
            {
                MutableRecord mutable = record.copy(false, true);
                do
                {
                    TemplateRecord.restoreItem(mutable, key);
                }
                while (TemplateRecord.getHiddenKeys(mutable).contains(key));
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

        InvalidHiddenKeyProblem that = (InvalidHiddenKeyProblem) o;

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
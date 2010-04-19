package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Identifies that an empty collection record is missing.  All collections
 * should be initialised as empty records.
 */
public class MissingCollectionProblem extends HealthProblemSupport
{
    private String key;

    public MissingCollectionProblem(String path, String message, String key)
    {
        super(path, message);
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        // If the collection is still missing, insert a blank record to
        // represent an empty collection.
        Record parent = recordManager.select(getPath());
        if (parent != null && !parent.containsKey(key))
        {
            recordManager.insert(PathUtils.getPath(getPath(), key), new MutableRecordImpl());
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

        MissingCollectionProblem that = (MissingCollectionProblem) o;

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
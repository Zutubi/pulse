package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Identifies an unexpected nested record under a record.
 */
public class UnexpectedNestedRecordProblem extends HealthProblemSupport
{
    private String key;

    /**
     * Creates a new problem for the given unexpected record at the given path.
     * 
     * @param path    path of the record the key is in
     * @param message message describing this problem
     * @param key     key of the unexpected record found
     */
    protected UnexpectedNestedRecordProblem(String path, String message, String key)
    {
        super(path, message);
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        // If the unexpected record is still there, just blow it away.
        String nestedPath = PathUtils.getPath(getPath(), key);
        if (recordManager.containsRecord(nestedPath))
        {
            recordManager.delete(nestedPath);
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

        UnexpectedNestedRecordProblem that = (UnexpectedNestedRecordProblem) o;

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
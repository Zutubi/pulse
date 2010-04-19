package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.Arrays;

/**
 * Identifies a reference that is not pulled up to the highest level.  All
 * references to inherited items should use the handle where that item is first
 * defined in the hierarchy.
 */
public class NonCanonicalReferenceProblem extends HealthProblemSupport
{
    private String key;
    private String referencedHandle;
    private String canonicalHandle;

    /**
     * Creates a new problem for the given non-canonical reference at the given
     * key of the record at the given path.
     * 
     * @param path             path of the record the reference is defined in
     * @param message          message describing this problem
     * @param key              the property where the reference was found
     * @param referencedHandle the invalid reference value
     * @param canonicalHandle  the canonical reference value
     */
    protected NonCanonicalReferenceProblem(String path, String message, String key, String referencedHandle, String canonicalHandle)
    {
        super(path, message);
        this.key = key;
        this.referencedHandle = referencedHandle;
        this.canonicalHandle = canonicalHandle;
    }

    public void solve(RecordManager recordManager)
    {
        // Replace all instances of the bad handle with the canonical one
        // provided on construction of this problem.
        Record record = recordManager.select(getPath());
        if (record != null)
        {
            Object value = record.get(key);
            if (value != null)
            {
                if (value instanceof String && value.equals(referencedHandle))
                {
                    MutableRecord mutableRecord = record.copy(false, true);
                    mutableRecord.put(key, canonicalHandle);
                    recordManager.update(getPath(), mutableRecord);
                }
                else if (value instanceof String[])
                {
                    String[] references = (String[]) value;
                    String[] canonicalised = CollectionUtils.mapToArray(references, new Mapping<String, String>()
                    {
                        public String map(String s)
                        {
                            if (s.equals(referencedHandle))
                            {
                                return canonicalHandle;
                            }
                            else
                            {
                                return s;
                            }
                        }
                    }, new String[references.length]);
                    
                    if (!Arrays.equals(references, canonicalised))
                    {
                        MutableRecord mutableRecord = record.copy(false, true);
                        mutableRecord.put(key, canonicalised);
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

        NonCanonicalReferenceProblem that = (NonCanonicalReferenceProblem) o;

        if (canonicalHandle != null ? !canonicalHandle.equals(that.canonicalHandle) : that.canonicalHandle != null)
        {
            return false;
        }
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
        result = 31 * result + (canonicalHandle != null ? canonicalHandle.hashCode() : 0);
        return result;
    }
}
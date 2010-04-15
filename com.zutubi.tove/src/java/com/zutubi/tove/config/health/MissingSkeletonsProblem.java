package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.RecordUtils;

/**
 * Indicates that skeleton records are missing.  This occurs where records are
 * present in the template parent, but absent in the child (and not hidden).
 */
public class MissingSkeletonsProblem extends HealthProblemSupport
{
    private String templateParentPath;
    private String key;

    /**
     * Creates a new problem indicating missing skeletons at the given key of
     * the given path.
     * 
     * @param path               path of the record the skeletons should be
     *                           under
     * @param message            description of this problem
     * @param templateParentPath path of the template parent of the record the
     *                           skeletons should be under
     * @param key                key where the skeletons should be
     */
    public MissingSkeletonsProblem(String path, String message, String templateParentPath, String key)
    {
        super(path, message);
        this.templateParentPath = templateParentPath;
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        String inheritedPath = PathUtils.getPath(templateParentPath, key);

        if (recordManager.containsRecord(getPath()) && recordManager.containsRecord(inheritedPath))
        {
            Record existingChild = recordManager.select(getPath());
            if (!existingChild.containsKey(key))
            {
                Record inherited = recordManager.select(inheritedPath);
                recordManager.insert(PathUtils.getPath(getPath(), key), RecordUtils.createSkeletonOf(inherited));
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

        MissingSkeletonsProblem that = (MissingSkeletonsProblem) o;

        if (key != null ? !key.equals(that.key) : that.key != null)
        {
            return false;
        }
        if (templateParentPath != null ? !templateParentPath.equals(that.templateParentPath) : that.templateParentPath != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (templateParentPath != null ? templateParentPath.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}

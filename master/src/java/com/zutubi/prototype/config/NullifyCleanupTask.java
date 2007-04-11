package com.zutubi.prototype.config;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.validation.i18n.TextProvider;

/**
 * A reference cleanup task that nulls out the reference.  Should only be
 * used when the reference is not required.
 */
public class NullifyCleanupTask extends ReferenceCleanupTaskSupport
{
    private RecordManager recordManager;

    public NullifyCleanupTask(String referencingPath, RecordManager recordManager)
    {
        super(referencingPath);
        this.recordManager = recordManager;
    }

    public void execute()
    {
        super.execute();
        
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        Record parentRecord = recordManager.load(parentPath);
        if (parentRecord != null)
        {
            MutableRecord newValue = parentRecord.copy(false);
            newValue.put(PathUtils.getBaseName(getAffectedPath()), "");
            recordManager.update(parentPath, newValue);
        }
    }

    public String getSummary(TextProvider textProvider)
    {
        return textProvider.getText("nullify.reference");
    }
}

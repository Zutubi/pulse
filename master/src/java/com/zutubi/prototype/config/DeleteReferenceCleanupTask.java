package com.zutubi.prototype.config;

import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.validation.i18n.TextProvider;

import java.util.Set;

/**
 * A reference cleanup task that deletes a record.  Any tasks needed to
 * cleanup references to the deleted record will cascade off this.
 */
public class DeleteReferenceCleanupTask extends ReferenceCleanupTaskSupport
{
    private RecordManager recordManager;

    public DeleteReferenceCleanupTask(String path, RecordManager recordManager)
    {
        super(path);
        this.recordManager = recordManager;
    }

    public void execute()
    {
        super.execute();
        recordManager.delete(getAffectedPath());
    }

    public String getSummary(TextProvider textProvider)
    {
        return textProvider.getText("delete.record");
    }

    @SuppressWarnings({"unchecked"})
    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
        paths.add(getAffectedPath());
    }
}

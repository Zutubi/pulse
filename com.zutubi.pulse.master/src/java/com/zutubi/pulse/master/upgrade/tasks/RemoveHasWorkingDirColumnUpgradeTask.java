package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;

/**
 * CIB-2365, remove the flag from the build result indicating that a working copy
 * has been captured.
 */
public class RemoveHasWorkingDirColumnUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    private static final String TABLE_BUILD_RESULT = "BUILD_RESULT";
    private static final String COLUMN_HAS_WORK_DIR = "hasWorkDir";

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws Exception
    {
        refactor.dropColumn(TABLE_BUILD_RESULT, COLUMN_HAS_WORK_DIR);
    }
}

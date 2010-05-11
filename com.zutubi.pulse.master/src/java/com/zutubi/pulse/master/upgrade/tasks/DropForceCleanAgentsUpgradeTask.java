package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.sql.Connection;

/**
 * Remove the old force clean agents table.
 */
public class DropForceCleanAgentsUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    private static final String TABLE_FORCE_CLEAN_AGENTS = "FORCE_CLEAN_AGENTS";

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws Exception
    {
        refactor.dropTable(TABLE_FORCE_CLEAN_AGENTS);
    }
}
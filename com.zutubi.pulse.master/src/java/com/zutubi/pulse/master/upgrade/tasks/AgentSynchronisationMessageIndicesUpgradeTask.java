package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds indices to queried columns on the AGENT_SYNCH_MESSAGE table.
 */
public class AgentSynchronisationMessageIndicesUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        addIndex(con, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_TYPE_NAME", "TYPE_NAME");
        addIndex(con, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_DESCRIPTION", "DESCRIPTION");
        addIndex(con, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_STATUS_NAME", "STATUS_NAME");
    }
}

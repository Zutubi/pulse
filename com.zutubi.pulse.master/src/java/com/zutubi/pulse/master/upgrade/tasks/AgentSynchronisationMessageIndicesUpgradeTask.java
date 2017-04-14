/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        addIndex(con, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_DESCRIPTION", "DESCRIPTION", 127);
        addIndex(con, "AGENT_SYNCH_MESSAGE", "AGENT_SYNCH_MESSAGE_STATUS_NAME", "STATUS_NAME");
    }
}

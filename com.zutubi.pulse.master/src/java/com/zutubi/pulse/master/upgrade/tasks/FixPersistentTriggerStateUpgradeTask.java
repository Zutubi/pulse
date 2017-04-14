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

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;

/**
 * This upgrade tasks fixes a bug with the persistent state of upgrade tasks. CIB-1670
 * <p/>
 * The problem is that scheduled triggers have the persistent state of NONE.  To fix,
 * go through and update all triggers with NONE (which should not be the case) and set
 * them to SCHEDULED.
 */
public class FixPersistentTriggerStateUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("UPDATE local_trigger SET state = 'SCHEDULED' where state = 'NONE'");
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }
}

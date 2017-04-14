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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This upgrade task increases the size limit on revision strings.
 */
public class RevisionStringLimitUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        dropIndex(con, "BUILD_RESULT", "idx_build_revision_string");
        dropIndex(con, "BUILD_CHANGELIST", "idx_changelist_revision_string");

        JDBCUtils.DbType dbType = JDBCUtils.getDBType(con);
        runUpdate(con, getUpdateStatement(dbType, "BUILD_RESULT", "REVISION_STRING"));
        runUpdate(con, getUpdateStatement(dbType, "BUILD_CHANGELIST", "REVISION_STRING"));
    }

    private String getUpdateStatement(JDBCUtils.DbType dbType, String table, String column) throws SQLException
    {

        switch (dbType)
        {
            case HSQL:
                return String.format("ALTER TABLE %s ALTER COLUMN %s varchar(8191)", table, column);
            case MYSQL:
                return String.format("ALTER TABLE %s MODIFY %s text(8191)", table, column);
            case POSTGRESQL:
                return String.format("ALTER TABLE %s ALTER COLUMN %s TYPE varchar(8191)", table, column);
        }

        throw new RuntimeException("Unknown database type " + dbType);
    }
}
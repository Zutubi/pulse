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
import com.zutubi.pulse.master.upgrade.UpgradeTask;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.io.File;

public class MigrateSchemaUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    protected List<String> getMappings()
    {
        // we are handling our own map processing.. 
        return new LinkedList<String>();
    }

    protected List<File> getTestFileMappings()
    {
        return new LinkedList<File>();
    }

    public void testAddTableWithColumn() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/master/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute();

        // after, tet that table is there.
        assertTrue(checkTableExists("TEST"));
        assertTrue(checkColumnExists("TEST", "NAME"));
    }

    public void testAddColumnToExistingTable() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/master/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute();

        // after, tet that table is there.
        assertFalse(checkColumnExists("TEST", "NEW_COLUMN"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/master/upgrade/tasks/testSchemaMigration-v2.hbm.xml");
        upgrade.execute();

        assertTrue(checkColumnExists("TEST", "NEW_COLUMN"));
    }

    private UpgradeTask newSchemaUpgrade(String mapping)
    {
        MigrateSchemaUpgradeTask task = new MigrateSchemaUpgradeTask();
        task.setMapping(mapping);
        task.setDataSource(dataSource);
        task.setDatabaseConfig(databaseConfig);
        return task;
    }

    private boolean checkTableExists(String tableName) throws SQLException
    {
        return JDBCUtils.tableExists(dataSource, tableName);
    }

    private boolean checkColumnExists(String tableName, String columnName) throws SQLException
    {
        return JDBCUtils.columnExists(dataSource, tableName, columnName);
    }
}
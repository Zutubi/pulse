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
import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.tove.type.record.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteTemplateProjectStateUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private DeleteTemplateProjectStateUpgradeTask task;
    private RecordManager recordManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        recordManager = mock(RecordManager.class);

        task = new DeleteTemplateProjectStateUpgradeTask();
        task.setDataSource(dataSource);
        task.setRecordManager(recordManager);
    }

    // this is somewhat artificial, but serves well enough as a boundry case check 
    public void testDeletionOfAllProjectStatesWhenNoProjectsConfigured() throws SQLException, UpgradeException
    {
        Map<String, Record> allProjects = new HashMap<String, Record>();
        doReturn(allProjects).when(recordManager).selectAll("projects/*");

        insertProject(1L, 2L, 3L);

        task.execute();

        assertEquals(0, getProjectTableRowCount());
    }

    public void testDeletionOfTemplateStates() throws SQLException, UpgradeException
    {
        Map<String, Record> allProjects = new HashMap<String, Record>();
        addConcreteProjectRecord(allProjects, "a", 1);
        addConcreteProjectRecord(allProjects, "b", 2);
        addTemplateProjectRecord(allProjects, "c");
        doReturn(allProjects).when(recordManager).selectAll("projects/*");

        insertProject(1L, 2L, 3L);

        task.execute();

        assertEquals(2, getProjectTableRowCount());
    }

    public void testDeletionOfUnknownStates() throws SQLException, UpgradeException
    {
        Map<String, Record> allProjects = new HashMap<String, Record>();
        addConcreteProjectRecord(allProjects, "a", 1);
        doReturn(allProjects).when(recordManager).selectAll("projects/*");

        insertProject(1L, 2L, 3L);

        task.execute();

        assertEquals(1, getProjectTableRowCount());
    }

    protected List<String> getMappings()
    {
        return Arrays.asList("com/zutubi/pulse/master/upgrade/tasks/schema/Schema-2.0.13-mappings.hbm.xml");
    }

    private void addConcreteProjectRecord(Map<String, Record> allProjects, String name, long projectId)
    {
        MutableRecord r = new MutableRecordImpl();
        r.put("name", name);
        r.put("projectId", Long.toString(projectId));
        r.putMeta(TemplateRecord.TEMPLATE_KEY, Boolean.FALSE.toString());
        allProjects.put("projects/" + name, r);
    }

    private void addTemplateProjectRecord(Map<String, Record> allProjects, String name)
    {
        MutableRecord r = new MutableRecordImpl();
        r.put("name", name);
        r.putMeta(TemplateRecord.TEMPLATE_KEY, Boolean.TRUE.toString());
        allProjects.put("projects/" + name, r);
    }

    private long getProjectTableRowCount() throws SQLException
    {
        return JDBCUtils.executeTableRowCount(dataSource, "PROJECT");
    }

    private void insertProject(Long... ids) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps = null;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("INSERT INTO PROJECT (id) values (?)");

            for (Long id : Arrays.asList(ids))
            {
                JDBCUtils.setLong(ps, 1, id);
                assertEquals(1, ps.executeUpdate());
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(ps);
            JDBCUtils.close(con);
        }
    }
}

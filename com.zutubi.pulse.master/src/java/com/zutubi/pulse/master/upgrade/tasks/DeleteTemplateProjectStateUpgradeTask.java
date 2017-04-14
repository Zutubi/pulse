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
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The pre 1.4 versions of Jabberwocky did not filter the project rows that mapped
 * to template projects.  This causes issues since template projects should not have
 * a state.  This upgrade task fixes this problem by removing those project states.
 */
public class DeleteTemplateProjectStateUpgradeTask extends DatabaseUpgradeTask
{
    /**
     * The key for the projectConfiguration.projectId field.
     */
    private static final String KEY_PROJECT_ID = "projectId";

    private static final UpgradeTaskMessages I18N = new UpgradeTaskMessages(DeleteTemplateProjectStateUpgradeTask.class);
    
    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        // Do not halt.  Pulse will still work although not for everyone.
        return false;
    }

    public void execute(Connection con) throws SQLException
    {
        List<Long> concreteProjectStateIds = getConcreteProjectStateIds();

        List<Long> stateIds = selectAllProjectStateIds(con);

        List<Long> stateIdsToDelete = new LinkedList<Long>();

        for (Long id : stateIds)
        {
            if (!concreteProjectStateIds.contains(id))
            {
                stateIdsToDelete.add(id);
            }
        }
        deleteSelectedProjectStates(con, stateIdsToDelete);
    }

    private List<Long> getConcreteProjectStateIds()
    {
        RecordLocator projectRecordLocator = RecordLocators.newPathPattern(PathUtils.getPath("projects/*"));
        Map<String, Record> projectRecords = projectRecordLocator.locate(recordManager);

        List<Long> concreteProjectIds = new LinkedList<Long>();

        for (Record r : projectRecords.values())
        {
            if (r.containsKey(KEY_PROJECT_ID))
            {
                Long id = Long.valueOf((String) r.get(KEY_PROJECT_ID));
                if (isConcrete(r))
                {
                    concreteProjectIds.add(id);
                }
            }
        }
        return concreteProjectIds;
    }

    private void deleteSelectedProjectStates(Connection con, List<Long> stateIdsToDelete) throws SQLException
    {
        PreparedStatement delete = null;
        try
        {
            delete = con.prepareStatement("DELETE FROM PROJECT WHERE id = ?");
            for (Long id : stateIdsToDelete)
            {
                delete.setLong(1, id);
                int rowcount = delete.executeUpdate();
                if (rowcount != 1)
                {
                    throw new SQLException("Failed to delete project '" + id + "' from the database. Update rowcount is " + rowcount);
                }
            }
        }
        finally
        {
            JDBCUtils.close(delete);
        }
    }

    private List<Long> selectAllProjectStateIds(Connection con) throws SQLException
    {
        List<Long> ids = new LinkedList<Long>();
        PreparedStatement select = null;
        ResultSet results = null;
        try
        {
            select = con.prepareStatement("SELECT id FROM PROJECT");
            results = select.executeQuery();
            while (results.next())
            {
                ids.add(results.getLong("id"));
            }
            return ids;
        }
        finally
        {
            JDBCUtils.close(select);
            JDBCUtils.close(results);
        }
    }

    private boolean isConcrete(Record record)
    {
        return !Boolean.valueOf(record.getMeta(TemplateRecord.TEMPLATE_KEY));
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}

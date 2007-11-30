package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.LinkedList;

import org.hibernate.mapping.Table;

/**
 * A refactor that blows away tables no longer used once changelists are flattened out.
 */
public class FlattenChangelistSchemaRefactorUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    public String getName()
    {
        return "Flatten changelists (drop tables)";
    }

    public String getDescription()
    {
        return "Drops tables no longer required after changelists are flattened";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void doRefactor(UpgradeContext context, Connection con, SchemaRefactor refactor) throws SQLException
    {
        List<Long> changelistRevisions = getChangelistRevisions(con);

        refactor.dropTable(con, refactor.getTable("CHANGELIST_RESULTS"));
        refactor.dropTable(con, refactor.getTable("CHANGELIST_PROJECTS"));
        refactor.dropTable(con, refactor.getTable("FILE_CHANGE"));
        refactor.dropTable(con, refactor.getTable("FILE_REVISION"));
        refactor.dropTable(con, refactor.getTable("CHANGELIST"));

        deleteRevisions(con, changelistRevisions);
    }

    private List<Long> getChangelistRevisions(Connection con) throws SQLException
    {
        PreparedStatement selectChangelistRevisions = null;
        ResultSet rs = null;

        List<Long> result = new LinkedList<Long>();
        try
        {
            selectChangelistRevisions = con.prepareStatement("SELECT REVISION.ID FROM REVISION, CHANGELIST WHERE REVISION.ID = CHANGELIST.REVISION_ID");
            rs = selectChangelistRevisions.executeQuery();
            while(rs.next())
            {
                result.add(rs.getLong(1));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(selectChangelistRevisions);
        }

        return result;
    }

    private void deleteRevisions(Connection con, List<Long> ids) throws SQLException
    {
        PreparedStatement deleteRevision = null;
        con.setAutoCommit(false);
        try
        {
            deleteRevision = con.prepareStatement("DELETE FROM REVISION WHERE ID = ?");
            long i = 0;
            for(Long id: ids)
            {
                deleteRevision.setLong(1, id);
                deleteRevision.executeUpdate();

                if((i++ % 1000) == 0)
                {
                    con.commit();
                }
            }
        }
        finally
        {
            con.setAutoCommit(true);
            JDBCUtils.close(deleteRevision);
        }
    }
}

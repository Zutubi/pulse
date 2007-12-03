package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Flattens changelist data into one heavily indexed table to allow more
 * efficient querying.
 */
public class FlattenChangelistUpgradeTask extends DatabaseUpgradeTask
{
    private PreparedStatement selectChangelists;
    private PreparedStatement insertChangelist;
    private PreparedStatement selectFileChange;
    private PreparedStatement insertFileChange;
    private PreparedStatement selectFileRevision;
    private PreparedStatement insertFileRevision;
    private long nextId;

    public String getName()
    {
        return "Flatten changelists (may take a long time)";
    }

    public String getDescription()
    {
        return "Flattens out the changelist data for more efficient querying";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
    {
        nextId = HibernateUtils.getNextId(con);
        try
        {
            addIndices(con);
            prepareStatements(con);
            con.setAutoCommit(false);
            processChangelists(con);
            con.setAutoCommit(true);
            HibernateUtils.ensureNextId(con, nextId);
        }
        finally
        {
            con.setAutoCommit(true);
            closePreparedStatements();
        }
    }

    private void addIndices(Connection con) throws SQLException
    {
        addIndex(con, "BUILD_CHANGELIST", "idx_changelist_revision_author", "REVISION_AUTHOR");
        addIndex(con, "BUILD_CHANGELIST", "idx_changelist_revision_date", "REVISION_DATE");
        addIndex(con, "BUILD_CHANGELIST", "idx_changelist_project_id", "PROJECT_ID");
        addIndex(con, "BUILD_CHANGELIST", "idx_changelist_result_id", "RESULT_ID");
    }

    private void prepareStatements(Connection con) throws SQLException
    {
        selectChangelists = con.prepareStatement("SELECT CHANGELIST.ID AS ID, CHANGELIST.SERVER_UID AS SERVER_UID, REVISION.AUTHOR AS AUTHOR, REVISION.COMMENT AS COMMENT, REVISION.DATE AS DATE, REVISION.BRANCH AS BRANCH, REVISION.REVISIONSTRING AS REVISION_STRING, BUILD_RESULT.PROJECT AS PROJECT_ID, BUILD_RESULT.ID AS RESULT_ID " +
                "FROM CHANGELIST_RESULTS, CHANGELIST, REVISION, BUILD_RESULT " +
                "WHERE CHANGELIST_RESULTS.CHANGELIST_ID = CHANGELIST.ID and CHANGELIST.REVISION_ID = REVISION.ID and BUILD_RESULT.ID = CHANGELIST_RESULTS.RESULT_ID " +
                "LIMIT 200 OFFSET ?");
        insertChangelist = con.prepareStatement("INSERT INTO BUILD_CHANGELIST VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        selectFileChange = con.prepareStatement("SELECT ID, ACTION, FILENAME, REVISION_ID, CHANGELIST_ID, ORDINAL FROM FILE_CHANGE WHERE CHANGELIST_ID = ?");
        insertFileChange = con.prepareStatement("INSERT INTO BUILD_FILE_CHANGE VALUES (?, ?, ?, ?, ?, ?)");

        selectFileRevision = con.prepareStatement("SELECT TYPE, REVISION, NUMBER FROM FILE_REVISION WHERE ID = ?");
        insertFileRevision = con.prepareStatement("INSERT INTO BUILD_FILE_REVISION VALUES (?, ?, ?, ?)");
    }

    private void closePreparedStatements()
    {
        JDBCUtils.close(selectChangelists);
        JDBCUtils.close(insertChangelist);
        JDBCUtils.close(selectFileChange);
        JDBCUtils.close(insertFileChange);
        JDBCUtils.close(selectFileRevision);
        JDBCUtils.close(insertFileRevision);
    }

    private void processChangelists(Connection con) throws SQLException, IOException
    {
        long changelistCount = runQueryForLong(con, "SELECT COUNT(*) FROM CHANGELIST_RESULTS, BUILD_RESULT WHERE CHANGELIST_RESULTS.RESULT_ID = BUILD_RESULT.ID");
        long startTime = System.currentTimeMillis();

        boolean found;
        long processed = 0;
        do
        {
            found = false;
            selectChangelists.setLong(1, processed);
            ResultSet rs = null;
            try
            {
                rs = selectChangelists.executeQuery();
                while (rs.next())
                {
                    found = true;
                    processChangelist(JDBCUtils.getLong(rs, "ID"), JDBCUtils.getString(rs, "AUTHOR"), JDBCUtils.getString(rs, "COMMENT"), JDBCUtils.getLong(rs, "DATE"), JDBCUtils.getString(rs, "BRANCH"), JDBCUtils.getString(rs, "REVISION_STRING"), JDBCUtils.getString(rs, "SERVER_UID"), JDBCUtils.getLong(rs, "PROJECT_ID"), JDBCUtils.getLong(rs, "RESULT_ID"));
                    processed++;
                }

                con.commit();

                if (found)
                {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double fractionDone = (double)processed / changelistCount;
                    double elapsedMinutes = elapsed / 60000.0;
                    double remainingMinutes = elapsedMinutes * (1 - fractionDone) / fractionDone;
                    System.out.format("Flatten Changelists Upgrade Task: Processed %d of %d changelists (%.02f%%); Elapsed %.02f minutes, estimated %.02f minutes remaining\n", processed, changelistCount, fractionDone * 100, elapsedMinutes, remainingMinutes);
                }
            }
            finally
            {
                JDBCUtils.close(rs);
            }

        } while(found);
    }

    private void processChangelist(Long id, String author, String comment, Long date, String branch, String revisionString, String uid, Long projectId, Long resultId) throws SQLException
    {
        long newId = nextId++;

        insertChangelist.setLong(1, newId);
        insertChangelist.setString(2, author);
        insertChangelist.setString(3, comment);
        insertChangelist.setLong(4, date);
        insertChangelist.setString(5, branch);
        insertChangelist.setString(6, revisionString);
        insertChangelist.setString(7, uid);
        insertChangelist.setLong(8, projectId);
        insertChangelist.setLong(9, resultId);
        insertChangelist.executeUpdate();

        duplicateFileChanges(id, newId);
    }

    private void duplicateFileChanges(Long changelistId, Long newChangelistId) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            selectFileChange.setLong(1, changelistId);
            rs = selectFileChange.executeQuery();
            while(rs.next())
            {
                long revisionId = duplicateFileRevision(rs.getLong("REVISION_ID"));
                insertFileChange.setLong(1, nextId++);
                insertFileChange.setString(2, rs.getString(2));
                insertFileChange.setString(3, rs.getString(3));
                insertFileChange.setLong(4, revisionId);
                insertFileChange.setLong(5, newChangelistId);
                insertFileChange.setInt(6, rs.getInt(6));
                insertFileChange.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private long duplicateFileRevision(long id) throws SQLException
    {
        long newId = nextId++;
        ResultSet rs = null;
        try
        {
            selectFileRevision.setLong(1, id);
            rs = selectFileRevision.executeQuery();
            if(rs.next())
            {
                insertFileRevision.setLong(1, newId);
                insertFileRevision.setString(2, rs.getString(1));
                insertFileRevision.setString(3, rs.getString(2));
                insertFileRevision.setLong(4, rs.getLong(3));
                insertFileRevision.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }

        return newId;
    }

}

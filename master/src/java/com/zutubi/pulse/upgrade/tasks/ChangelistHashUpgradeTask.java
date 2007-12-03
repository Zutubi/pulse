package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Adds a hash column to the changelist table for fast lookup of equivalent
 * changelists.
 */
public class ChangelistHashUpgradeTask extends DatabaseUpgradeTask
{
    private PreparedStatement selectChangelists;
    private PreparedStatement setHash;

    public String getName()
    {
        return "Changelist hash";
    }

    public String getDescription()
    {
        return "Adds a hash column to the changelist table for faster lookup";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
    {
        try
        {
            fixIndices(con);
            prepareStatements(con);
            con.setAutoCommit(false);
            processChangelists(con);
            con.setAutoCommit(true);
        }
        finally
        {
            con.setAutoCommit(true);
            closePreparedStatements();
        }
    }

    private void fixIndices(Connection con) throws SQLException
    {
        try
        {
            dropIndex(con, "BUILD_CHANGELIST", "idx_changelist_uid_rev");
        }
        catch(SQLException e)
        {
            // It may not exist, so ignore this error
        }

        addIndex(con, "BUILD_CHANGELIST", "idx_changelist_hash", "HASH");
    }

    private void prepareStatements(Connection con) throws SQLException
    {
        selectChangelists = con.prepareStatement("SELECT ID, REVISION_AUTHOR, REVISION_COMMENT, REVISION_DATE, REVISION_BRANCH, REVISION_STRING FROM BUILD_CHANGELIST ORDER BY ID LIMIT 200 OFFSET ?");
        setHash = con.prepareStatement("UPDATE BUILD_CHANGELIST SET HASH = ? WHERE ID = ?");
    }

    private void closePreparedStatements()
    {
        JDBCUtils.close(selectChangelists);
        JDBCUtils.close(setHash);
    }

    private void processChangelists(Connection con) throws SQLException, IOException
    {
        long changelistCount = runQueryForLong(con, "SELECT COUNT(*) FROM BUILD_CHANGELIST");
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
                    processChangelist(JDBCUtils.getLong(rs, "ID"), JDBCUtils.getString(rs, "REVISION_AUTHOR"), JDBCUtils.getString(rs, "REVISION_COMMENT"), JDBCUtils.getLong(rs, "REVISION_DATE"), JDBCUtils.getString(rs, "REVISION_BRANCH"), JDBCUtils.getString(rs, "REVISION_STRING"));
                    processed++;
                }

                con.commit();

                if (found)
                {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double fractionDone = (double)processed / changelistCount;
                    double elapsedMinutes = elapsed / 60000.0;
                    double remainingMinutes = elapsedMinutes * (1 - fractionDone) / fractionDone;
                    System.out.format("Changelist Hash Upgrade Task: Processed %d of %d changelists (%.02f%%); Elapsed %.02f minutes, estimated %.02f minutes remaining\n", processed, changelistCount, fractionDone * 100, elapsedMinutes, remainingMinutes);
                }
            }
            finally
            {
                JDBCUtils.close(rs);
            }

        } while(found);
    }

    private void processChangelist(Long id, String author, String comment, Long date, String branch, String revisionString) throws SQLException
    {
        String hash = getHash(author, comment, date, branch, revisionString);
        setHash.setString(1, hash);
        setHash.setLong(2, id);
        int i = setHash.executeUpdate();
        System.out.println("i = " + i);
    }

    public String getHash(String author, String comment, Long date, String branch, String revisionString)
    {
        String input = safeString(Long.toString(safeLong(date)) + "/" + safeString(author) + "/" + safeString(branch) + "/" + safeString(comment) + "/" + safeString(revisionString));
        return DigestUtils.md5Hex(input);
    }

    private long safeLong(Long l)
    {
        return l == null ? 0 : l;
    }

    private String safeString(String in)
    {
        if(in == null)
        {
            return "";
        }
        else
        {
            return in;
        }
    }
}

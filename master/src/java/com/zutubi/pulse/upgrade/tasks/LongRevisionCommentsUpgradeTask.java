package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Trims revision comments that are longer than the maximum allowed size of
 * 4k (see CIB-1130).
 */
public class LongRevisionCommentsUpgradeTask extends DatabaseUpgradeTask
{
    private static final int MAX_COMMENT_LENGTH = 4095;
    private static final String COMMENT_TRIM_MESSAGE = "... [trimmed]";

    public String getName()
    {
        return "Trim revision comments";
    }

    public String getDescription()
    {
        return "Trims revision comments that are longer than the maximum allowed length (CIB-1130).";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement("SELECT ID, COMMENT FROM REVISION");
            rs = stmt.executeQuery();
            while (rs.next())
            {
                trimComment(con, JDBCUtils.getLong(rs, "ID"), JDBCUtils.getString(rs, "COMMENT"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private String trimComment(Connection con, Long id, String comment) throws SQLException
    {
        if(comment != null && comment.length() > MAX_COMMENT_LENGTH)
        {
            comment = comment.substring(0, MAX_COMMENT_LENGTH - COMMENT_TRIM_MESSAGE.length()) + COMMENT_TRIM_MESSAGE;
            PreparedStatement stmt = null;
            try
            {
                stmt = con.prepareStatement("UPDATE REVISION SET COMMENT = ? WHERE ID = ?");
                stmt.setString(1, comment);
                stmt.setLong(2, id);
                stmt.executeUpdate();
            }
            finally
            {
                JDBCUtils.close(stmt);
            }
        }

        return comment;
    }
}

package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * <class-comment/>
 */
public class HibernateUtils
{
    /**
     * Emulate hibernates hilo key generation.
     *
     * @param con
     *
     * @return
     *
     * @throws SQLException
     */
    public static long getNextId(Connection con) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long nextHi;

        try
        {
            ps = con.prepareStatement("select next_hi from hibernate_unique_key");
            rs = ps.executeQuery();
            rs.next();
            nextHi = rs.getLong(1);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        try
        {
            ps = con.prepareStatement("UPDATE hibernate_unique_key SET next_hi = ?");
            JDBCUtils.setLong(ps, 1, nextHi + 1);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }

        return getNextId(nextHi);
    }

    private static long getNextId(long nextHi)
    {
        // Emulate Hibernate's hilo algorithm
        return nextHi * (Short.MAX_VALUE + 1) + 1;
    }

    public static void ensureNextId(Connection con, long lastId) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        long nextHi;

        try
        {
            ps = con.prepareStatement("select NEXT_HI from HIBERNATE_UNIQUE_KEY");
            rs = ps.executeQuery();
            rs.next();
            nextHi = rs.getLong(1);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        boolean updateRequired = false;
        while(lastId >= getNextId(nextHi))
        {
            updateRequired = true;
            nextHi++;
        }

        if (updateRequired)
        {
            try
            {
                ps = con.prepareStatement("UPDATE hibernate_unique_key SET next_hi = ?");
                JDBCUtils.setLong(ps, 1, nextHi);
                ps.executeUpdate();
            }
            finally
            {
                JDBCUtils.close(ps);
            }
        }
    }
}

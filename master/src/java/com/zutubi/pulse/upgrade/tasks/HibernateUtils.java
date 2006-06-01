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
        long nextId;

        try
        {
            ps = con.prepareStatement("select NEXT_HI from HIBERNATE_UNIQUE_KEY");
            rs = ps.executeQuery();
            rs.next();
            nextId = rs.getLong(1);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        try
        {
            nextId++;
            ps = con.prepareStatement("UPDATE hibernate_unique_key SET next_hi = ? where next_hi = ?");
            JDBCUtils.setLong(ps, 1, nextId);
            JDBCUtils.setLong(ps, 2, (nextId - 1));
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }

        // Emulate Hibernate's hilo algorithm
        return nextId * (Short.MAX_VALUE + 1) + 1;
    }

}

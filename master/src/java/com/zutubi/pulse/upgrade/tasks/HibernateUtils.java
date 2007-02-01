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

        try
        {
            nextHi++;
            ps = con.prepareStatement("UPDATE hibernate_unique_key SET next_hi = ? where next_hi = ?");
            JDBCUtils.setLong(ps, 1, nextHi);
            JDBCUtils.setLong(ps, 2, (nextHi - 1));
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

/*
    public static void dropConstraints(Configuration config, DataSource dataSource, String targetTableName) throws SQLException
    {
        if (!config.getTableMappings().hasNext())
        {
            config.buildMappings();
        }

        Iterator i = config.getTableMappings();
        while (i.hasNext())
        {
            Table t = (Table) i.next();
            Iterator fki = t.getForeignKeyIterator();
            while (fki.hasNext())
            {
                ForeignKey fk = (ForeignKey) fki.next();
                Table referencedTable = fk.getReferencedTable();
                if (referencedTable != null && referencedTable.getName().equals(targetTableName))
                {
                    fk.sqlDropString(null, null, null);
                    fk.setReferencedTable(null);
                    fk.sqlCreateString(null, null, null, null);
                }
                else
                {
                    System.out.println("skipping fk, no referenced table available... " + fk.getName());
                }
            }
        }
    }

    public static void copyTable(DataSource dataSource, String source, String destination) throws SQLException
    {
        JDBCUtils.execute(dataSource, "insert into " + destination + " values (select * from " + source + ")");
    }

    public static void copyColumn(DataSource dataSource, String tableName, String source, String destination) throws SQLException
    {
        JDBCUtils.execute(dataSource, "update "+tableName+" set " + destination + " = " + source);
    }
*/
}

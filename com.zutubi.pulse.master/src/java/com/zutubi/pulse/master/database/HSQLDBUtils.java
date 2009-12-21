package com.zutubi.pulse.master.database;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 *
 */
public class HSQLDBUtils
{
    private static final Logger LOG = Logger.getLogger(HSQLDBUtils.class);

    private static final long TOTAL_DB_SPACE = (long)Integer.MAX_VALUE * 8;

    public static void shutdown(DataSource dataSource) throws SQLException
    {
        JDBCUtils.execute(dataSource, "SHUTDOWN SCRIPT");
    }

    public static boolean updateMaxSizeRequired(DataSource dataSource) throws SQLException
    {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            con = dataSource.getConnection();
            stmt = con.prepareCall("SELECT property_value FROM information_schema.system_properties WHERE property_name = 'hsqldb.cache_file_scale'");
            rs = stmt.executeQuery();
            if(rs.next())
            {
                Long scale = JDBCUtils.getLong(rs, "property_value");
                if(scale != null && scale == 1)
                {
                    return true;
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
        }

        return false;
    }

    public static void updateMaxSize(String url) throws IOException
    {
        if (!url.startsWith("jdbc:hsqldb:"))
        {
            throw new IllegalArgumentException("Unexpected embedded hsqldb url: " + url);
        }
        String databasePath = url.substring(12);
        File databasePropertiesFile = new File(databasePath + ".properties");
        
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try
        {
            inStream = new FileInputStream(databasePropertiesFile);
            Properties properties = new Properties();
            properties.load(inStream);
            properties.put("hsqldb.cache_file_scale", "8");
            inStream.close();

            outStream = new FileOutputStream(databasePropertiesFile);
            properties.store(outStream, "Updated cache_file_scale");
        }
        finally
        {
            IOUtils.close(inStream);
            IOUtils.close(outStream);
        }
    }

    public static double getDatabaseUsagePercent(DataSource dataSource)
    {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            con = dataSource.getConnection();
            stmt = con.prepareCall("SELECT file_free_pos FROM information_schema.system_cacheinfo");
            rs = stmt.executeQuery();
            if(rs.next())
            {
                Long freePos = JDBCUtils.getLong(rs, "file_free_pos");
                if(freePos != null)
                {
                    return freePos * 100.0 / TOTAL_DB_SPACE;
                }
            }
        }
        catch (SQLException e)
        {
            LOG.severe(e);
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
        }

        return -1.0;
    }

    public static void compactDatabase(DataSource dataSource)
    {
        try
        {
            JDBCUtils.execute(dataSource, "CHECKPOINT DEFRAG");
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }
    }
}

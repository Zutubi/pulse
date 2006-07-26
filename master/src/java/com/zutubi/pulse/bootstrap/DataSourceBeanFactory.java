package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.JDBCUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public class DataSourceBeanFactory implements FactoryBean, ApplicationContextAware
{
    private ApplicationContext context;

    private BasicDataSource dataSource;

    public Object getObject() throws Exception
    {
        if (dataSource == null)
        {
            synchronized (this)
            {
                if (dataSource == null)
                {
                    createDataSource();
                    if(maxSizeUpdateRequired())
                    {
                        updateMaxSize();
                    }
                }
            }
        }
        return dataSource;
    }

    private void createDataSource()
    {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(getDriverClassName());
        dataSource.setUrl(getUrl());
        dataSource.setUsername(getUsername());
        dataSource.setPassword(getPassword());
    }

    private boolean maxSizeUpdateRequired()
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
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
        }

        return false;
    }

    private void updateMaxSize() throws SQLException
    {
        JDBCUtils.execute(dataSource, "SHUTDOWN SCRIPT");
        close();

        File dbPropertiesFile = new File(getDbRoot(), "db.properties");
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try
        {
            inStream = new FileInputStream(dbPropertiesFile);
            Properties properties = new Properties();
            properties.load(inStream);
            properties.put("hsqldb.cache_file_scale", "8");
            inStream.close();

            outStream = new FileOutputStream(dbPropertiesFile);
            properties.store(outStream, "Updated cache_file_scale");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.close(inStream);
            IOUtils.close(outStream);
        }

        createDataSource();
    }

    public String getDriverClassName()
    {
        return "org.hsqldb.jdbcDriver";
    }

    public String getUrl()
    {
        File dbRoot = getDbRoot();
        return "jdbc:hsqldb:" + dbRoot.getAbsolutePath() + File.separator + "db";
    }

    private File getDbRoot()
    {
        MasterConfigurationManager configManager = (MasterConfigurationManager) context.getBean("configurationManager");
        File dbRoot = configManager.getUserPaths().getDatabaseRoot();
        return dbRoot;
    }

    public String getUsername()
    {
        return "sa";
    }

    public String getPassword()
    {
        return "";
    }

    public Class getObjectType()
    {
        return DataSource.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.context = applicationContext;
    }

    public void close() throws SQLException
    {
        if (dataSource != null)
        {
            dataSource.close();
        }
    }
}

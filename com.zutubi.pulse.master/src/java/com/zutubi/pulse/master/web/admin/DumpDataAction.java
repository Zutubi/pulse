package com.zutubi.pulse.master.web.admin;

import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.transfer.TransferAPI;
import com.zutubi.pulse.master.web.ActionSupport;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

/**
 */
public class DumpDataAction extends ActionSupport
{
    private String file;
    private DataSource dataSource;
    private DatabaseConfig databaseConfig;
    private List<String> mappings;

    public void setFile(String file)
    {
        this.file = file;
    }

    public void setHibernateMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }

    public String execute() throws Exception
    {
        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addClassPathMappings(mappings);

        configuration.setProperties(databaseConfig.getHibernateProperties());

        TransferAPI transferAPI = new TransferAPI();
        transferAPI.dump(configuration, dataSource, new File(file));

        return SUCCESS;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
    }
}

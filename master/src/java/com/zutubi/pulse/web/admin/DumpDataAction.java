package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.transfer.TransferAPI;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.List;
import java.io.File;

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

        for (String mapping : mappings)
        {
            Resource resource = new ClassPathResource(mapping);
            configuration.addInputStream(resource.getInputStream());
        }

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

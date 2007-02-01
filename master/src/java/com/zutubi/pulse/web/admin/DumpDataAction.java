package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.web.ActionSupport;
import org.hsqldb.util.TransferAPI;

import javax.sql.DataSource;
import java.io.File;

/**
 */
public class DumpDataAction extends ActionSupport
{
    private String file;
    private DataSource dataSource;

    public void setFile(String file)
    {
        this.file = file;
    }

    public String execute() throws Exception
    {
        TransferAPI api = new TransferAPI();
        api.dump(dataSource, new File(file));
        return SUCCESS;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
}

/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.admin;

import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.transfer.TransferAPI;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

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

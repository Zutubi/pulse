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

package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("zutubi.setupConfig")
public class SetupConfiguration extends AbstractConfiguration
{
    private SetupDataConfiguration data;
    private SetupDatabaseTypeConfiguration databaseType;
    private MigrateDatabaseTypeConfiguration migrateDatabaseType;
    private AdminUserConfiguration admin;
    private ServerSettingsConfiguration server;

    public SetupDataConfiguration getData()
    {
        return data;
    }

    public void setData(SetupDataConfiguration data)
    {
        this.data = data;
    }

    public SetupDatabaseTypeConfiguration getDatabaseType()
    {
        return databaseType;
    }

    public void setDatabaseType(SetupDatabaseTypeConfiguration databaseType)
    {
        this.databaseType = databaseType;
    }

    public MigrateDatabaseTypeConfiguration getMigrateDatabaseType()
    {
        return migrateDatabaseType;
    }

    public void setMigrateDatabaseType(MigrateDatabaseTypeConfiguration migrateDatabaseType)
    {
        this.migrateDatabaseType = migrateDatabaseType;
    }

    public AdminUserConfiguration getAdmin()
    {
        return admin;
    }

    public void setAdmin(AdminUserConfiguration admin)
    {
        this.admin = admin;
    }

    public ServerSettingsConfiguration getServer()
    {
        return server;
    }

    public void setServer(ServerSettingsConfiguration server)
    {
        this.server = server;
    }
}

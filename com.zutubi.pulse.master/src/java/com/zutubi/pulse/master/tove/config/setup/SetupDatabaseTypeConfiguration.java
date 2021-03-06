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

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.io.IOUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.File;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.annotations.Required;

import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * Used for the database type setup page: i.e. the second step in a normal
 * setup procedure.
 */
@SymbolicName("zutubi.setupDatabaseTypeConfig")
@Form(fieldOrder = {"type", "driverFile", "host", "port", "database", "user", "password"})
public class SetupDatabaseTypeConfiguration extends AbstractConfiguration implements Validateable
{
    @ControllingSelect(enableSet = {"MYSQL", "POSTGRESQL", "RUBBISH"})
    @FieldScript
    private DatabaseType type = DatabaseType.EMBEDDED;

    @FieldAction(template = "SetupDatabaseTypeConfiguration.driverFile")
    @Required
    @File(verifyFile = true, verifyReadable = true)
    private String driverFile;

    @Required
    private String host = "localhost";

    @Numeric(min = 1)
    private int port;

    @Required
    private String database;

    @Required
    private String user;
    
    @Password
    private String password;

    public DatabaseType getType()
    {
        return type;
    }

    public void setType(DatabaseType type)
    {
        this.type = type;
    }

    public String getDriverFile()
    {
        return driverFile;
    }

    public void setDriverFile(String driverFile)
    {
        this.driverFile = driverFile;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void validate(ValidationContext context)
    {
        if (type != DatabaseType.EMBEDDED)
        {
            JarFile jar = null;
            try
            {
                jar = new JarFile(driverFile, true);
            }
            catch (IOException e)
            {
                context.addFieldError("driverFile", e.getMessage());
            }
            finally
            {
                IOUtils.close(jar);
            }
        }
    }

    @Transient
    public Properties getDatabaseProperties()
    {
        return getType().getDatabaseProperties(this);
    }
}

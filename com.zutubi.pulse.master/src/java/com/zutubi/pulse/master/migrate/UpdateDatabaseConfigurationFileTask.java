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

package com.zutubi.pulse.master.migrate;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.util.monitor.AbstractTask;
import com.zutubi.pulse.master.util.monitor.TaskException;

import java.io.IOException;
import java.util.Properties;

/**
 *
 *
 */
public class UpdateDatabaseConfigurationFileTask extends AbstractTask
{
    private Properties updatedConfiguration;

    private MasterConfigurationManager configurationManager;

    public UpdateDatabaseConfigurationFileTask(String name)
    {
        super(name);
    }

    public void setUpdatedConfiguration(Properties updatedConfiguration)
    {
        this.updatedConfiguration = updatedConfiguration;
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        try
        {
            configurationManager.updateDatabaseConfig(updatedConfiguration);
        }
        catch (IOException e)
        {
            throw new TaskException(e);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}

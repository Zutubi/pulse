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

package com.zutubi.pulse.master.bootstrap;

import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.database.DriverRegistry;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Extended configuration only present on the Pulse master (not agents).  The main additions are
 * the database and artifact repositories.
 */
public interface MasterConfigurationManager extends ConfigurationManager, DataResolver
{
    String CONFIG_DIR = ".pulse2";
    
    MasterUserPaths getUserPaths();

    void setPulseData(File pulseHome);

    File getDataDirectory();

    Data getData();

    File getHomeDirectory();

    File getDatabaseConfigFile();

    void updateDatabaseConfig(Properties updatedProperties) throws IOException;

    DatabaseConfig getDatabaseConfig() throws IOException;

    DriverRegistry getDriverRegistry();
}

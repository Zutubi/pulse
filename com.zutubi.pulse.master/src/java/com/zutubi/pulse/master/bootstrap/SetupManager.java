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

import com.zutubi.pulse.master.bootstrap.tasks.ProcessSetupStartupTask;
import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.pulse.master.tove.config.setup.*;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import java.io.IOException;

/**
 * The setup manager is a state based process handler that controls the setup of pulse.  The setup process is started
 * by calling #startSetupWorkflow. If user interaction is required, the setup manager will pause, and wait for the
 * appropriate requestXXXComplete method to be called. The current state of the setup process can be retrieved
 * via the #getCurrentState method. 
 *
 */
public interface SetupManager
{
    SetupState getCurrentState();

    String getStatusMessage();

    void init(ProcessSetupStartupTask processSetupStartupTask);

    void startSetupWorkflow();

    SetupDataConfiguration getDefaultData() throws IOException;

    void setData(SetupDataConfiguration data) throws IOException;

    void setDatabaseType(SetupDatabaseTypeConfiguration db) throws IOException;

    void executeMigrate(MigrateDatabaseTypeConfiguration config);

    void abortMigrate();

    void postMigrate();

    void executeRestore();

    void abortRestore();

    void postRestore();

    void executeUpgrade();

    void postUpgrade();

    UserConfiguration setAdminUser(AdminUserConfiguration admin) throws Exception;

    ServerSettingsConfiguration getDefaultServerSettings();

    void setServerSettings(ServerSettingsConfiguration settings) throws Exception;
}

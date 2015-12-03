package com.zutubi.pulse.master.bootstrap;

import com.zutubi.pulse.master.bootstrap.tasks.ProcessSetupStartupTask;
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

    void migrateComplete();

    void executeRestore();

    void abortRestore();

    void postRestore();

    void setLicense(SetupLicenseConfiguration license);

    void requestUpgradeComplete(boolean changes);

    UserConfiguration setAdminUser(AdminUserConfiguration admin) throws Exception;

    ServerSettingsConfiguration getDefaultServerSettings();

    void setServerSettings(ServerSettingsConfiguration settings) throws Exception;
}

package com.zutubi.pulse.bootstrap;

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

    void requestDataComplete() throws IOException;

    void requestLicenseComplete();

    void requestUpgradeComplete();

    void requestSetupComplete();

    void startSetupWorkflow(ProcessSetupStartupTask processSetupStartupTask);
}

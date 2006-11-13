package com.zutubi.pulse.bootstrap;

import java.io.IOException;

/**
 * <class-comment/>
 */
public interface SetupManager
{
    SetupState getCurrentState();

    void startSetupWorkflow() throws IOException;

    void requestDataComplete() throws IOException;

    void requestLicenseComplete();

    void requestUpgradeComplete();

    void requestSetupComplete();
}

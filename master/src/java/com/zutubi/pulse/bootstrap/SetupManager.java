package com.zutubi.pulse.bootstrap;

/**
 * <class-comment/>
 */
public interface SetupManager
{
    SetupState getCurrentState();

    void startSetupWorkflow();

    void requestDataComplete();

    void requestLicenseComplete();

    void requestUpgradeComplete();

    void requestSetupComplete();
}

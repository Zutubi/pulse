package com.zutubi.pulse.servercore.services;

/**
 */
public enum UpgradeState
{
    /**
     * Not currently upgrading, or waiting for the first message from the
     * agent.
     */
    INITIAL,
    /**
     * Underway.
     */
    STARTED,
    /**
     * Downloading a new package.
     */
    DOWNLOADING,
    /**
     * Applying a new package.
     */
    APPLYING,
    /**
     * The plugins are being brought into line with the master.
     */
    SYNCHRONISING_PLUGINS,
    /**
     * Rebooting.  Expect to be able to ping me in the morning!
     */
    REBOOTING,
    /**
     * The upgrade completed without the need for a reboot.
     */
    COMPLETE,
    /**
     * Upgrade failed because of changes detected to non-upgradeable
     * components.  Manual upgrade is required.
     */
    FAILED,
    /**
     * Some unexpected error occurred during the upgrade.
     */
    ERROR
}

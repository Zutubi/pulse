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
     * Rebooting.  Expect to be able to ping me in the morning!
     */
    REBOOTING,
    /**
     * Upgrade failed because of changes detected to non-upgradeable
     * components.  Manual upgrade is required.
     */
    FAILED,
    /**
     * Some unexpected error occured during the upgrade.
     */
    ERROR
}

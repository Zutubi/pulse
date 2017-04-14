package com.zutubi.pulse.master.bootstrap;

/**
 * States reached through the setup process.
 */
public enum SetupState
{
    WAITING,
    DATA,
    DATABASE,
    MIGRATE,
    RESTORE,
    UPGRADE,
    ADMIN,
    SETTINGS,
    STARTING,
}

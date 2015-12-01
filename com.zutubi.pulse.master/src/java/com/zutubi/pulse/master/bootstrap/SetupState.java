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
    LICENSE,
    UPGRADE,
    ADMIN,
    SETTINGS,
    STARTING,
}

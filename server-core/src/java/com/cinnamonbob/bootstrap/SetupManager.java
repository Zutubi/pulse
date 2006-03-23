package com.cinnamonbob.bootstrap;

/**
 * The SetupManager handled the systems setup process.
 *
 */
public interface SetupManager
{
    /**
     * Run through the systems setup procedure.
     *
     */
    void setup();

    /**
     * Indicates whether or not the system has been setup.
     *
     * @return true if the system is setup, false otherwise.
     */
    boolean isSetup();
}

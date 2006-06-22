package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public interface LicenseManager
{
    /**
     * Returns true if the installed license enables the core build system.
     * 
     */
    boolean canBuild();

    /**
     * Returns true if the installed license allows another project to be created.
     *
     */
    boolean canAddProject();

    /**
     * Returns true if the installed license allows another user to be created.
     *
     */
    boolean canAddUser();

    /**
     * Returns true if the installed license allows another agent to be configured.
     *
     */
    boolean canAddAgent();
}

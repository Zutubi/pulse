package com.zutubi.pulse.master.model;

/**
 * A configuration object that allows the build cleanu process to be
 * fine tuned.
 */
public class BuildCleanupOptions
{
    public static final BuildCleanupOptions WORKD_DIR_ONLY = new BuildCleanupOptions(true, false, false);
    public static final BuildCleanupOptions DATABASE_ONLY = new BuildCleanupOptions(false, false, true);
    public static final BuildCleanupOptions ALL = new BuildCleanupOptions(false, true, true);
    public static final BuildCleanupOptions EXCEPT_DATABASE = new BuildCleanupOptions(true, true, false);
    
    private boolean cleanWorkDir = true;
    private boolean cleanBuildDir = true;
    private boolean cleanDatabase = true;

    private BuildCleanupOptions(boolean cleanWorkDir, boolean cleanBuildDir, boolean cleanDatabase)
    {
        this.cleanWorkDir = cleanWorkDir;
        this.cleanBuildDir = cleanBuildDir;
        this.cleanDatabase = cleanDatabase;
    }

    public boolean isCleanWorkDir()
    {
        return (!cleanBuildDir) && cleanWorkDir;
    }

    public boolean isCleanBuildDir()
    {
        return cleanBuildDir;
    }

    public boolean isCleanDatabase()
    {
        return cleanDatabase;
    }
}

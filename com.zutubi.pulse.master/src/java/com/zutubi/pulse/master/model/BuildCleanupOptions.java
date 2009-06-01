package com.zutubi.pulse.master.model;

/**
 * A configuration object that allows you to fine tune exactly which portion of
 * a build is cleaned up.
 *
 * This object implements its own value based equals and hashcode functions so that
 * two options objects with the same settings will be considered as equal. 
 */
public class BuildCleanupOptions
{
    private boolean cleanWorkDir;
    private boolean cleanBuildArtifacts;
    private boolean cleanRepositoryArtifacts;
    private boolean cleanDatabase;

    public BuildCleanupOptions()
    {
    }

    public BuildCleanupOptions(boolean cleanWorkDir, boolean cleanBuildArtifacts, boolean cleanRepositoryArtifacts, boolean cleanDatabase)
    {
        this.cleanWorkDir = cleanWorkDir;
        this.cleanBuildArtifacts = cleanBuildArtifacts;
        this.cleanRepositoryArtifacts = cleanRepositoryArtifacts;
        this.cleanDatabase = cleanDatabase;
    }

    /**
     * Indicated whether or not a builds working copy should be cleaned up.
     * Note, this has no effect on builds that have not retained their working
     * copy.
     *
     * @return true if the working copy will be cleaned up.
     */
    public boolean isCleanWorkDir()
    {
        return cleanWorkDir;
    }

    public void setCleanWorkDir(boolean cleanWorkDir)
    {
        this.cleanWorkDir = cleanWorkDir;
    }

    /**
     * Indicates whether or not a builds captured artifacts should be cleaned up.  This
     * includes artifacts such as the environment listing as well as anything captured
     * during the build.
     *
     * @return true if the builds artifacts will be cleaned up.
     */
    public boolean isCleanBuildArtifacts()
    {
        return cleanBuildArtifacts;
    }

    public void setCleanBuildArtifacts(boolean cleanBuildArtifacts)
    {
        this.cleanBuildArtifacts = cleanBuildArtifacts;
    }

    /**
     * Indicates whether or not artifacts that have been published to the internal artifact
     * repository should be cleaned up.  These artifacts are distinct from the regular build
     * artifacts in that they are made available to other project builds.
     *
     * @return true if the repository artifacts will be cleaned up.
     */
    public boolean isCleanRepositoryArtifacts()
    {
        return cleanRepositoryArtifacts;
    }

    public void setCleanRepositoryArtifacts(boolean cleanRepositoryArtifacts)
    {
        this.cleanRepositoryArtifacts = cleanRepositoryArtifacts;
    }

    /**
     * Indicates whether or not the build should be removed from the database.  Once removed
     * from the database, the build will no longer appear in reports and listings.
     *
     * @return true if the build should be removed from the database.
     */
    public boolean isCleanDatabase()
    {
        return cleanDatabase;
    }

    public void setCleanDatabase(boolean cleanDatabase)
    {
        this.cleanDatabase = cleanDatabase;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildCleanupOptions that = (BuildCleanupOptions) o;

        if (cleanBuildArtifacts != that.cleanBuildArtifacts) return false;
        if (cleanDatabase != that.cleanDatabase) return false;
        if (cleanRepositoryArtifacts != that.cleanRepositoryArtifacts) return false;
        if (cleanWorkDir != that.cleanWorkDir) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (cleanWorkDir ? 1 : 0);
        result = 31 * result + (cleanBuildArtifacts ? 1 : 0);
        result = 31 * result + (cleanRepositoryArtifacts ? 1 : 0);
        result = 31 * result + (cleanDatabase ? 1 : 0);
        return result;
    }
}

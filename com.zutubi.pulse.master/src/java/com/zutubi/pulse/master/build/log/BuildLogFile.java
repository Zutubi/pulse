package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

/**
  * Convenient specialisation of {@link LogFile} for accessing build logs.
  */
public class BuildLogFile extends LogFile
{
    public static final String LOG_FILENAME = "build.log";

    /**
     * Creates a new {@link LogFile} for accessing a build log.
     *
     * @param build build to access the log of
     * @param paths used to locate build directories
     */
    public BuildLogFile(BuildResult build, MasterBuildPaths paths)
    {
        super(new File(paths.getBuildDir(build), LOG_FILENAME), build.getProject().getConfig().getOptions().isLogCompressionEnabled());
    }
}

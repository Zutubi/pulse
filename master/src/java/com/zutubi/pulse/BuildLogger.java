package com.zutubi.pulse;

import com.zutubi.pulse.model.BuildResult;

/**
 *
 *
 */
public interface BuildLogger extends HookLogger
{
    /**
     * Initialise any required resources.  This method will be called before any logging
     * requestes are made.
     */
    void prepare();

    void preBuild();
    void preBuildCompleted();

    void commenced(BuildResult build);

    void status(String message);

    void completed(BuildResult build);

    void postBuild();
    void postBuildCompleted();

    /**
     * Close any held resources.  This method will be called after the final logging
     * request is made.
     */
    void done();

}

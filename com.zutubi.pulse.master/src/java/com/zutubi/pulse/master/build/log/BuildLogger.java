package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.master.model.BuildResult;
import org.apache.ivy.util.MessageLogger;

public interface BuildLogger extends HookLogger
{
    void preamble(BuildResult build);
    
    void preBuild();
    void preBuildCompleted();

    void commenced(BuildResult build);

    void status(String message);

    void completed(BuildResult build);

    void postBuild();
    void postBuildCompleted();

    /**
     * Called prior to Ivy dependency resolution.
     */
    void preIvyResolve();
    /**
     * Called after Ivy dependency resolution.
     *
     * @param errors all problem messages reported by Ivy, empty on successful resolve
     */
    void postIvyResolve(String... errors);

    void preIvyPublish();
    void postIvyPublish(String... errors);

    MessageLogger getMessageLogger();
}

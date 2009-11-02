package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.master.model.BuildResult;
import org.apache.ivy.util.MessageLogger;

public interface BuildLogger extends HookLogger
{
    void preBuild();
    void preBuildCompleted();

    void commenced(BuildResult build);

    void status(String message);

    void completed(BuildResult build);

    void postBuild();
    void postBuildCompleted();

    void preIvyPublish();
    void postIvyPublish();

    MessageLogger getMessageLogger();
}

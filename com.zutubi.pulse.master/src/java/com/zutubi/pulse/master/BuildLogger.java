package com.zutubi.pulse.master;

import com.zutubi.pulse.master.model.BuildResult;

public interface BuildLogger extends HookLogger
{
    void preBuild();
    void preBuildCompleted();

    void commenced(BuildResult build);

    void status(String message);

    void completed(BuildResult build);

    void postBuild();
    void postBuildCompleted();


}

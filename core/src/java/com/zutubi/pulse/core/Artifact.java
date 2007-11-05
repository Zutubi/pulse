package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

/**
 */
public interface Artifact
{
    String getName();

    /**
     * Called to actually capture the artifacts, adding them to the build
     * result and potentially moving files to a permanent storage location.
     *
     * @param result  command we are capturing artifacts from
     * @param context context in which the commmand was run
     */
    void capture(CommandResult result, ExecutionContext context);
}

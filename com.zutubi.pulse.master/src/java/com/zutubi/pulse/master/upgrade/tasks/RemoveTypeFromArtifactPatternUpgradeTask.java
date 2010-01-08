package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;

/**
 * Remove the type portion of the artifact pattern from the artifacts in the
 * artifact repository.
 */
public class RemoveTypeFromArtifactPatternUpgradeTask extends RefactorArtifactRepositoryUpgradeTask
{
    private static final String EXISTING_ARTIFACT_PATTERN = "([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[ext]";
    private static final String NEW_ARTIFACT_PATTERN = "([organisation]/)[module]/([stage]/)[artifact](-[revision])(.[ext])";
    
    private MasterUserPaths userPaths;

    public void execute() throws TaskException
    {
        setRepositoryBase(userPaths.getRepositoryRoot());
        execute(EXISTING_ARTIFACT_PATTERN, NEW_ARTIFACT_PATTERN);
    }

    public void setUserPaths(MasterUserPaths userPaths)
    {
        this.userPaths = userPaths;
    }
}

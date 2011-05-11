package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.util.monitor.TaskException;

/**
 * Remove the type portion of the artifact pattern from the artifacts in the
 * artifact repository.
 */
public class RemoveTypeFromArtifactPatternUpgradeTask extends RefactorArtifactRepositoryUpgradeTask
{
    private static final String EXISTING_ARTIFACT_PATTERN = "([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[ext]";
    private static final String NEW_ARTIFACT_PATTERN = "([organisation]/)[module]/([stage]/)[artifact](-[revision])(.[ext])";
    
    private MasterConfigurationManager configurationManager;

    public void execute() throws TaskException
    {
        setRepositoryBase(configurationManager.getUserPaths().getRepositoryRoot());
        execute(EXISTING_ARTIFACT_PATTERN, NEW_ARTIFACT_PATTERN);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}

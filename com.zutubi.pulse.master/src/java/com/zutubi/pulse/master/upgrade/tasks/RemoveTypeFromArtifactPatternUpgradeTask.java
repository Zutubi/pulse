/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

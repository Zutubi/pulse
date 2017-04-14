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

package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.api.ScmClientFactory} to handle the
 * creation of the Git ScmClient.
 *
 * @see com.zutubi.pulse.core.scm.git.GitClient
 */
public class GitClientFactory implements ScmClientFactory<GitConfiguration>
{
    public ScmClient createClient(GitConfiguration config) throws ScmException
    {
        int inactivityTimeout = config.isInactivityTimeoutEnabled() ? config.getInactivityTimeoutSeconds() : 0;
        boolean processSubmodules = config.getSubmoduleProcessing() != GitConfiguration.SubmoduleProcessing.NONE;
        GitClient client = new GitClient(config.getRepository(), config.getBranch(), inactivityTimeout,config.getCloneType(),
                                         config.getCloneDepth(), config.getMasterCloneDepth(), processSubmodules, getSelectedSubmodules(config));
        client.setFilterPaths(config.getIncludedPaths(), config.getExcludedPaths());
        return client;
    }

    private List<String> getSelectedSubmodules(GitConfiguration config)
    {
        String submodules = config.getSelectedSubmodules();
        if (submodules == null)
        {
            return Collections.emptyList();
        }
        else
        {
            submodules = submodules.trim();
            return Arrays.asList(submodules.split("\\s+"));
        }
    }
}

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
        GitClient client = new GitClient(config.getRepository(), config.getBranch(), inactivityTimeout, config.getCloneType(), processSubmodules, getSelectedSubmodules(config));
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

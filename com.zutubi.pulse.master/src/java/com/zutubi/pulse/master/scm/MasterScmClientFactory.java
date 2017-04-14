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

package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.variables.ConfigurationVariableProvider;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.logging.Logger;

/**
 * An ScmClientFactory that adds master-only capabilities on top of {@link DelegateScmClientFactory}.
 *
 * Really I think ScmClientFactory is overworked and should cleaned up.  Plugins probably need not
 * implement factories (they are pretty trivial, instead ScmClient types could be required to have
 * a constructor taking their ScmConfiguration type). Then ScmClientFactory could be used
 * internally only.
 */
public class MasterScmClientFactory extends DelegateScmClientFactory
{
    private static final Logger LOG = Logger.getLogger(MasterScmClientFactory.class);

    private ConfigurationProvider configurationProvider;
    private ConfigurationVariableProvider configurationVariableProvider;

    /**
     * Preferred way to create an ScmClient on the master when a project is known. The project is
     * used as context for resolving variables in the SCM configuration.
     *
     * Note: we don't get the SCM from the project, as it is valid for the project to be null, and
     * there are cases were the SCM is not yet saved but we want to use it (e.g. testing config in
     * the UI).
     *
     * @param project project owning the SCM
     * @param config the SCM configuration used to create the client
     * @return a client for the given configuration
     * @throws ScmException on error
     */
    public ScmClient createClient(ProjectConfiguration project, ScmConfiguration config) throws ScmException
    {
        if (project != null)
        {
            VariableMap variables = configurationVariableProvider.variablesForConfiguration(project);
            config = configurationVariableProvider.resolveStringProperties(config, variables);
        }
        return super.createClient(config);
    }

    @Override
    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        ProjectConfiguration project = null;
        if (config.getConfigurationPath() != null)
        {
            try
            {
                project = configurationProvider.getAncestorOfType(config, ProjectConfiguration.class);
            }
            catch (Exception e)
            {
                // Ignore.  This can happen if e.g. the project has been renamed, which is one
                // reason we generally prefer the project to be passed in when creating clients
                // rather than looked up in this way.
                LOG.debug(e);
            }
        }

        return createClient(project, config);
    }

    public void setConfigurationVariableProvider(ConfigurationVariableProvider configurationVariableProvider)
    {
        this.configurationVariableProvider = configurationVariableProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}

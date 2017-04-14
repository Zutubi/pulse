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

import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.UnaryFunctionE;
import com.zutubi.util.io.IOUtils;

import java.util.Set;

/**
 * Utility methods for working with {@link com.zutubi.pulse.core.scm.api.ScmClient}
 * instances.
 */
public class ScmClientUtils
{
    /**
     * Executes an action with an ScmClient created from the given
     * configuration.  Takes care of creation and cleanup of the client.
     *
     * @param scmConfiguration configuration used to instantiate the client
     * @param clientFactory    factory for creating the client
     * @param action           callback to run with the client
     * @param <T>              type returned by the callback, and thus this method
     * @return the result of the callback
     * @throws ScmException if the callback encounters an error
     */
    public static <T> T withScmClient(ProjectConfiguration project, ScmConfiguration scmConfiguration, MasterScmClientFactory clientFactory, ScmAction<T> action) throws ScmException
    {
        ScmClient client = null;
        try
        {
            client = clientFactory.createClient(project, scmConfiguration);
            return action.process(client);
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    /**
     * Executes an action with an ScmClient created from the given
     * configuration and an ScmContext with no persistent context.  Use only
     * when no project is available.
     *
     * @param scmConfiguration configuration used to create the client
     * @param scmManager   factory for creating the client and context
     * @param action       callback to run with the client and context
     * @return the result of the callback
     * @throws ScmException if the callback encounters an error
     */
    public static <T> T withScmClient(ScmConfiguration scmConfiguration, ScmManager scmManager, ScmContextualAction<T> action) throws ScmException
    {
        ScmClient client = null;
        try
        {
            client = scmManager.createClient(null, scmConfiguration);
            ScmContext context = scmManager.createContext(client.getImplicitResource());
            return action.process(client, context);
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    /**
     * Executes an action with an ScmClient created from the given
     * configuration and an ScmContext.  Takes care of creation and cleanup of
     * the client and context.
     * <p/>
     * Note that the project configuration and state are passed independently
     * instead of passing a Project entity because the project configuration
     * may have been frozen some time ago (so getting it from the entity
     * would be incorrect).
     *
     * @param project      configuration used to create the client and context
     * @param projectState state of the project
     * @param scmManager   factory for creating the client and context
     * @param action       callback to run with the client and context
     * @return the result of the callback
     * @throws ScmException if the callback encounters an error
     */
    public static <T> T withScmClient(ProjectConfiguration project, Project.State projectState, ScmManager scmManager, ScmContextualAction<T> action) throws ScmException
    {
        ScmClient client = null;
        try
        {
            ScmConfiguration scm = project.getScm();
            client = scmManager.createClient(project, scm);
            ScmContext context = scmManager.createContext(project, projectState, client.getImplicitResource());
            return action.process(client, context);
        }
        finally
        {
            IOUtils.close(client);
        }
    }

    public static Set<ScmCapability> getCapabilities(ProjectConfiguration config, Project.State projectState, ScmManager manager) throws ScmException
    {
        return withScmClient(config, projectState, manager, new ScmContextualAction<Set<ScmCapability>>()
        {
            public Set<ScmCapability> process(ScmClient client, ScmContext context) throws ScmException
            {
                return client.getCapabilities(context);
            }
        });
    }
    
    public static interface ScmAction<T> extends UnaryFunctionE<ScmClient, T, ScmException>
    {
    }

    public static interface ScmContextualAction<T>
    {
        T process(ScmClient client, ScmContext context) throws ScmException;
    }
}

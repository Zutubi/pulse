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

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * The scm manager handles the basic management of the background scm processes
 * and life cycle.
 */
public interface ScmManager extends MasterScmContextFactory
{
    ScmClient createClient(ProjectConfiguration project, ScmConfiguration config) throws ScmException;

    /**
     * Clears any cached information for the project with the given id.  This
     * should be called whenever the SCM details for a project change
     * significantly.  Note that a context lock for the given project must be
     * held when calling this method.
     *
     * @param projectId the project to clear the cache for
     */
    void clearCache(long projectId);
}

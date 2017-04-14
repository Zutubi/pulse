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

package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.xwork.actions.project.ProjectActionSupport;
import com.zutubi.util.time.TimeStamps;

import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;

/**
 * Simple ajax action to retrieve the latest revision for a project, used on
 * the build properties editing page (prompt on trigger).
 */
public class GetLatestRevisionAction extends ProjectActionSupport
{
    private ScmManager scmManager;

    private GetLatestRevisionActionResult result;

    public GetLatestRevisionActionResult getResult()
    {
        return result;
    }

    public String execute()
    {
        result = new GetLatestRevisionActionResult();

        final Project project = getProject();
        if(project == null)
        {
            result.setError("Unknown project");
        }
        else
        {
            try
            {
                result.setLatestRevision(withScmClient(project.getConfig(), project.getState(), scmManager, new ScmContextualAction<String>()
                {
                    public String process(ScmClient client, ScmContext context) throws ScmException
                    {
                        if(client.getCapabilities(context).contains(ScmCapability.REVISIONS))
                        {
                            return client.getLatestRevision(context).getRevisionString();
                        }
                        else
                        {
                            return new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), getLocale())).getRevisionString();
                        }
                    }
                }));

                result.setSuccessful(true);
            }
            catch (Exception e)
            {
                result.setError(e.toString());
            }
        }
        
        return SUCCESS;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}

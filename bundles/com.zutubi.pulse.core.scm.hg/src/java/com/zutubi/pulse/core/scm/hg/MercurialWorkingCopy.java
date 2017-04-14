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

package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.process.api.ScmLineHandlerSupport;
import com.zutubi.pulse.core.scm.process.api.ScmOutputHandlerToUIAdapter;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implements personal build support for Mercurial.
 */
public class MercurialWorkingCopy implements WorkingCopy
{
    public Set<WorkingCopyCapability> getCapabilities()
    {
        return EnumSet.allOf(WorkingCopyCapability.class);
    }

    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        return true;
    }

    public Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException
    {
        MercurialCore core = createCore(context);
        String branch = core.branch();
        String incoming = core.incoming(branch);
        if (incoming == null)
        {
            // We must be already up to date.
            List<Changelist> changes = core.log(false, null, branch, branch, 1);
            if (changes.isEmpty())
            {
                throw new ScmException("Unable to get remote revision: up-to-date but no log entries");
            }
            
            return changes.get(0).getRevision();
        }
        else
        {
            return new Revision(incoming);
        }
    }

    public Revision guessLocalRevision(WorkingCopyContext context) throws ScmException
    {
        return new Revision(createCore(context).parents());
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        MercurialCore core = createCore(context);
        ScmLineHandlerSupport handler = new ScmOutputHandlerToUIAdapter(context.getUI());
        core.pull(handler, null);
        core.update(handler, safeRevisionString(revision));
        return new Revision(core.parents());
    }

    private String safeRevisionString(Revision revision)
    {
        return revision == null ? null : revision.getRevisionString();
    }

    private MercurialCore createCore(WorkingCopyContext context)
    {
        MercurialCore core = new MercurialCore();
        core.setWorkingDirectory(context.getBase());
        return core;
    }
}

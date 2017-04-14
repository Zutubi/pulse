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

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.project.events.ProjectEvent;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * This event indicates that a change has been detected in an SCM.
 */
public class ScmChangeEvent extends ProjectEvent
{
    private Revision newRevision;
    private Revision previousRevision;

    public ScmChangeEvent(ProjectConfiguration source, Revision newRevision, Revision previousRevision)
    {
        super(source, source);
        this.newRevision = newRevision;
        this.previousRevision = previousRevision;
    }

    public Revision getNewRevision()
    {
        return newRevision;
    }

    public Revision getPreviousRevision()
    {
        return previousRevision;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("SCM Change Event");
        if (getSource() != null)
        {
            buff.append(": ").append((getProjectConfiguration()).getName());
        }
        buff.append(": ").append(getPreviousRevision()).append(" -> ").append(getNewRevision());
        return buff.toString();
    }
}
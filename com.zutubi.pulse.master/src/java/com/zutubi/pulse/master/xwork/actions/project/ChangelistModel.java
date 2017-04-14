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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;

import java.util.Collection;

/**
 * JSON model for a changelist.  Note that the file changes are not included
 * directly, they are not always needed and in some cases may be trimmed to
 * a limit.
 */
public class ChangelistModel
{
    private long id;
    private RevisionModel revision;
    private String who;
    private DateModel when;
    private ChangelistCommentModel comment;

    public ChangelistModel(PersistentChangelist changelist, ProjectConfiguration projectConfig, Collection<CommitMessageTransformerConfiguration> transformers)
    {
        id = changelist.getId();
        revision = new RevisionModel(changelist.getRevision(), projectConfig);
        who = changelist.getAuthor();
        when = new DateModel(changelist.getTime());
        comment = new ChangelistCommentModel(new CommitMessageSupport(changelist.getComment(), transformers));
    }

    public long getId()
    {
        return id;
    }

    public RevisionModel getRevision()
    {
        return revision;
    }

    public String getWho()
    {
        return who;
    }

    public DateModel getWhen()
    {
        return when;
    }

    public ChangelistCommentModel getComment()
    {
        return comment;
    }
}

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.committransformers.CommitMessageSupport;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
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

    public ChangelistModel(PersistentChangelist changelist, ChangeViewerConfiguration changeViewer, Collection<CommitMessageTransformerConfiguration> transformers)
    {
        id = changelist.getId();
        revision = new RevisionModel(changelist.getRevision(), changeViewer);
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

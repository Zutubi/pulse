package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.committransformers.CommitMessageSupport;

/**
 * JSON model for a changelist comment, includes an abbreviated version.
 */
public class ChangelistCommentModel
{
    private static final int COMMENT_LINE_LENGTH = 80;
    private static final int COMMENT_TRIM_LIMIT = 60;

    private String abbreviated;
    private String comment;

    public ChangelistCommentModel(CommitMessageSupport commitMessageSupport)
    {
        if (commitMessageSupport.getLength() > COMMENT_TRIM_LIMIT)
        {
            abbreviated = commitMessageSupport.trim(COMMENT_TRIM_LIMIT);
        }

        this.comment = commitMessageSupport.wrap(COMMENT_LINE_LENGTH);
    }

    public String getAbbreviated()
    {
        return abbreviated;
    }

    public String getComment()
    {
        return comment;
    }
}

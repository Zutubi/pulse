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

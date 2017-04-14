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

package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.xwork.actions.project.DateModel;

/**
 * UI model for comments (on builds and agents).
 */
public class CommentModel
{
    private long id;
    private String message;
    private String author;
    private DateModel date;
    private boolean canDelete;

    public CommentModel(Comment comment, boolean canDelete)
    {
        id = comment.getId();
        message = comment.getMessage();
        author = comment.getAuthor();
        date = new DateModel(comment.getTime());
        this.canDelete = canDelete;
    }

    public long getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getAuthor()
    {
        return author;
    }

    public DateModel getDate()
    {
        return date;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }
}

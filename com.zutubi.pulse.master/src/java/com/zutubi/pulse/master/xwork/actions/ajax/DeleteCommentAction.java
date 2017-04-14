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

import static com.google.common.collect.Iterables.find;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.EntityWithIdPredicate;
import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.model.CommentContainer;
import com.zutubi.pulse.master.model.User;
import com.zutubi.tove.security.AccessManager;

/**
 * Action allowing a user to delete a comment on a build result.
 */
public class DeleteCommentAction extends CommentActionBase
{
    private static final Messages I18N = Messages.getInstance(DeleteCommentAction.class);

    private long commentId;

    public void setCommentId(long commentId)
    {
        this.commentId = commentId;
    }

    @Override
    protected void updateContainer(CommentContainer container, User user)
    {
        Comment comment = find(container.getComments(), new EntityWithIdPredicate<Comment>(commentId), null);
        if (comment == null)
        {
            throw new IllegalArgumentException(I18N.format("unknown.comment", commentId));
        }

        accessManager.ensurePermission(AccessManager.ACTION_DELETE, comment);
        container.removeComment(comment);
    }
}
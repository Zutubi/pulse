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

package com.zutubi.pulse.master.model;

import java.util.List;

/**
 * Abstraction over entities that have a collection of comments.
 */
public interface CommentContainer
{
    String ACTION_ADD_COMMENT = "addComment";

    /**
     * @return the comments on this container
     */
    List<Comment> getComments();

    /**
     * Adds a new comment to the end of the list.
     *
     * @param comment the comment to add
     */
    void addComment(Comment comment);

    /**
     * Deletes a comment, if it exists.
     *
     * @param comment the comment to delete
     * @return true if the comment was found and deleted, false if it was not found
     */
    boolean removeComment(Comment comment);
}

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

import com.zutubi.pulse.master.model.Comment;
import com.zutubi.util.StringUtils;
import flexjson.JSON;

import java.util.LinkedList;
import java.util.List;

/**
 * Models a summary of recent comments for a build, suitable to give a high level
 * idea of the presence and gist of comments.
 */
public class CommentSummaryModel
{
    private static final int MAX_COMMENTS = 3;

    private int commentCount;
    private List<CommentSnippetModel> recentComments = new LinkedList<CommentSnippetModel>();

    public CommentSummaryModel(List<Comment> comments)
    {
        commentCount = comments.size();
        for(Comment recent: comments.subList(Math.max(0, commentCount - MAX_COMMENTS), commentCount))
        {
            recentComments.add(0, new CommentSnippetModel(recent));
        }
    }

    public int getCommentCount()
    {
        return commentCount;
    }

    @JSON
    public List<CommentSnippetModel> getRecentComments()
    {
        return recentComments;
    }

    public static class CommentSnippetModel
    {
        private static final int MAX_SNIPPET_LENGTH = 128;

        private String author;
        private String snippet;

        public CommentSnippetModel(Comment comment)
        {
            this.author = comment.getAuthor();
            this.snippet = StringUtils.trimmedString(comment.getMessage(), MAX_SNIPPET_LENGTH);
        }

        public String getAuthor()
        {
            return author;
        }

        public String getSnippet()
        {
            return snippet;
        }
    }
}

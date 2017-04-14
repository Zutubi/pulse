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

package com.zutubi.pulse.master.committransformers;

import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A helper class that applies multiple transforms to a commit comment.
 */
public class CommitMessageSupport
{
    private static final Logger LOG = Logger.getLogger(CommitMessageSupport.class);

    private String comment;
    private Collection<CommitMessageTransformerConfiguration> transformers;

    public CommitMessageSupport(String comment, Collection<CommitMessageTransformerConfiguration> transformers)
    {
        this.transformers = transformers;
        this.comment = comment == null ? "" : comment;
    }

    protected CommitMessageBuilder applyTransformers()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder(comment);
        List<Substitution> substitutions = new LinkedList<Substitution>();
        for (CommitMessageTransformerConfiguration transformer : transformers)
        {
            for (Substitution substitution: transformer.substitutions())
            {
                if (!substitutions.contains(substitution))
                {
                    substitutions.add(substitution);
                }
            }
        }

        for (Substitution substitution: substitutions)
        {
            builder.replace(substitution);
        }
        
        return builder;
    }

    public int getLength()
    {
        try
        {
            return applyTransformers().getLength();
        }
        catch (Exception e)
        {
            LOG.warning(String.format("Failed to process changelist comment. Cause: %s", e.getMessage()), e);
            return comment.length();
        }
    }

    public String trim(int length)
    {
        try
        {
            CommitMessageBuilder builder = applyTransformers();
            builder.trim(length);
            builder.encode();
            return builder.toString();
        }
        catch (Exception e)
        {
            LOG.warning(String.format("Failed to process changelist comment. Cause: %s", e.getMessage()), e);
            return comment;
        }
    }

    public String wrap(int length)
    {
        try
        {
            CommitMessageBuilder builder = applyTransformers();
            builder.wrap(length);
            builder.encode();
            return builder.toString();
        }
        catch (Exception e)
        {
            LOG.warning(String.format("Failed to process changelist comment. Cause: %s", e.getMessage()), e);
            return comment;
        }
    }

    public String toString()
    {
        try
        {
            CommitMessageBuilder builder = applyTransformers();
            builder.encode();
            return builder.toString();
        }
        catch (Exception e)
        {
            LOG.warning(String.format("Failed to process changelist comment. Cause: %s", e.getMessage()), e);
            return comment;
        }
    }
}

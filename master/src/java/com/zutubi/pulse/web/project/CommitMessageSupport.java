package com.zutubi.pulse.web.project;

import com.zutubi.pulse.committransformers.CommitMessageBuilder;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;

/**
 * <class comment/>
 */
public class CommitMessageSupport
{
    private static final Logger LOG = Logger.getLogger(CommitMessageSupport.class);

    private Changelist changelist;
    private List<CommitMessageTransformer> transformers;
    private ChangelistDao changelistDao;

    public CommitMessageSupport(Changelist changelist, List<CommitMessageTransformer> transformers, ChangelistDao changelistDao)
    {
        this.transformers = transformers;
        this.changelist = changelist;
        this.changelistDao = changelistDao;
    }

    protected CommitMessageBuilder applyTransformers()
    {
        CommitMessageBuilder builder = new CommitMessageBuilder(changelist.getComment());
        for (CommitMessageTransformer transformer : transformers)
        {
            if (transformer.appliesToChangelist(changelistDao.getAllAffectedProjectIds(changelist)))
            {
                builder = transformer.transform(builder);
            }
        }
        return builder;
    }

    public int getLength()
    {
        return applyTransformers().getLength();
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
            return changelist.getComment();
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
            return changelist.getComment();
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
            return changelist.getComment();
        }
    }
}

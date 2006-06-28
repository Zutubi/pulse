package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.util.StringUtils;

import java.util.List;

/**
 */
public class CommitMessageHelper
{
    private List<CommitMessageTransformer> transformers;

    public CommitMessageHelper(ProjectManager projectManager)
    {
        transformers = projectManager.getCommitMessageTransformers();
    }

    public String applyTransforms(Changelist changelist)
    {
        return applyTransforms(changelist, 0);
    }
    
    public String applyTransforms(Changelist changelist, int limit)
    {
        String result = changelist.getComment();
        if(limit > 0)
        {
            result = StringUtils.trimmedString(result, limit);
        }

        result = TextUtils.htmlEncode(result);
        for(CommitMessageTransformer transformer: transformers)
        {
            if(transformer.appliesToChangelist(changelist))
            {
                result = transformer.transform(result);
            }
        }

        return result;
    }
}

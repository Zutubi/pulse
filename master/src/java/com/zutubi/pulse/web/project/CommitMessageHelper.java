package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.util.StringUtils;

import java.util.List;

/**
 */
public class CommitMessageHelper
{
    private List<CommitMessageTransformer> transformers;

    public CommitMessageHelper(List<CommitMessageTransformer> transformers)
    {
        this.transformers = transformers;
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
        else
        {
            //CIB-726: attempt to limit lines to no more than 80 characters long.
            result = StringUtils.wrapString(result, 80, null, false);
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

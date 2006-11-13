package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 * <class comment/>
 */
public class CustomCommitMessageTransformer extends CommitMessageTransformer
{
    private static final String EXPRESSION_PROPERTY = "custom.expression";
    private static final String REPLACEMENT_PROPERTY = "custom.replacement";

    public String getExpression()
    {
        return getProperties().getProperty(EXPRESSION_PROPERTY);
    }

    public void setExpression(String expression)
    {
        getProperties().setProperty(EXPRESSION_PROPERTY, expression);
    }

    public String getReplacement()
    {
        return getProperties().getProperty(REPLACEMENT_PROPERTY);
    }

    public void setReplacement(String replacement)
    {
        getProperties().setProperty(REPLACEMENT_PROPERTY, replacement);
    }

    public String getType()
    {
        return "Custom";
    }

    public CommitMessageBuilder transform(CommitMessageBuilder builder)
    {
        builder.replace(getExpression(), getReplacement());
        return builder;
    }
}

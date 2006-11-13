package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 * <class comment/>
 */
public class LinkCommitMessageTransformer extends CommitMessageTransformer
{
    public static final String EXPRESSION_PROPERTY = "standard.expression";
    public static final String LINK_PROPERTY = "standard.link";

    public LinkCommitMessageTransformer()
    {
    }

    public LinkCommitMessageTransformer(String name)
    {
        setName(name);
    }

    public LinkCommitMessageTransformer(String name, String expression, String link)
    {
        setName(name);
        setExpression(expression);
        setLink(link);
    }

    public String getExpression()
    {
        return getProperties().getProperty(EXPRESSION_PROPERTY);
    }

    public void setExpression(String expression)
    {
        getProperties().setProperty(EXPRESSION_PROPERTY, expression);
    }

    public String getLink()
    {
        return getProperties().getProperty(LINK_PROPERTY);
    }

    public void setLink(String link)
    {
        getProperties().setProperty(LINK_PROPERTY, link);
    }

    public String getType()
    {
        return "Link";
    }

    public CommitMessageBuilder transform(CommitMessageBuilder builder)
    {
        String replacement = "<a href='" + getLink() + "'>$0</a>";
        builder.replace(getExpression(), replacement);
        return builder;
    }
}

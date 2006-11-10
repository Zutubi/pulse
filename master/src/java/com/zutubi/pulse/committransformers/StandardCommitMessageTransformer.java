package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.util.logging.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <class comment/>
 */
public class StandardCommitMessageTransformer extends CommitMessageTransformer
{
    private static final Logger LOG = Logger.getLogger(StandardCommitMessageTransformer.class);

    public static final String EXPRESSION_PROPERTY = "standard.expression";
    public static final String LINK_PROPERTY = "standard.link";

    private Pattern pattern;

    public StandardCommitMessageTransformer()
    {
    }

    public StandardCommitMessageTransformer(String name)
    {
        setName(name);
    }

    public StandardCommitMessageTransformer(String name, String expression, String link)
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
        return "Standard";
    }

    public String transform(String message)
    {
        if (pattern == null)
        {
            pattern = Pattern.compile(getExpression());
        }

        Matcher matcher = pattern.matcher(message);
        String r = "<a href='" + getLink() + "'>$0</a>";

        try
        {
            return matcher.replaceAll(r);
        }
        catch (IndexOutOfBoundsException e)
        {
            LOG.warning("Unable to apply commit message link '" + getName() + "': " + e.getMessage(), e);
            return message;
        }
    }
}

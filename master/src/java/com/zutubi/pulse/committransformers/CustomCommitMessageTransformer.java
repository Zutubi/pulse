package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.util.logging.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <class comment/>
 */
public class CustomCommitMessageTransformer extends CommitMessageTransformer
{
    private static final Logger LOG = Logger.getLogger(CustomCommitMessageTransformer.class);

    private static final String EXPRESSION_PROPERTY = "custom.expression";
    private static final String REPLACEMENT_PROPERTY = "custom.replacement";

    private Pattern pattern;

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

    public String transform(String message)
    {
        if (pattern == null)
        {
            pattern = Pattern.compile(getExpression());
        }

        Matcher matcher = pattern.matcher(message);
        String r = getReplacement();

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

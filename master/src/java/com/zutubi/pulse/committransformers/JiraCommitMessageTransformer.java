package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.util.logging.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <class comment/>
 */
public class JiraCommitMessageTransformer extends CommitMessageTransformer
{
    private static final Logger LOG = Logger.getLogger(JiraCommitMessageTransformer.class);

    private static final String URL_PROPERTY = "jira.url";

    private Pattern pattern;

    public void setUrl(String url)
    {
        getProperties().setProperty(URL_PROPERTY, url);
    }

    public String getUrl()
    {
        return getProperties().getProperty(URL_PROPERTY);
    }

    public String getType()
    {
        return "Jira";
    }

    public String transform(String message)
    {
        if (pattern == null)
        {
            pattern = Pattern.compile("([a-zA-Z]+-[0-9]+)");
        }

        String url = getUrl();
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        Matcher matcher = pattern.matcher(message);
        String r = String.format("<a href='%s/browse/$1'>$1</a>", url);

        try
        {
            return matcher.replaceAll(r);
        }
        catch (IndexOutOfBoundsException e)
        {
            LOG.warning("Unable to apply commit message link '" + getName() + "': " + e.getMessage(), e);
            return message;
        }
    }}

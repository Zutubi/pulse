package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.model.CommitMessageTransformer;

/**
 * <class comment/>
 */
public class JiraCommitMessageTransformer extends CommitMessageTransformer
{
    private static final String URL_PROPERTY = "jira.url";

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

    public CommitMessageBuilder transform(CommitMessageBuilder builder)
    {
        String url = getUrl();
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        String replacement = String.format("<a href='%s/browse/$1'>$1</a>", url);
        builder.replace("([a-zA-Z]+-[0-9]+)", replacement);

        return builder;
    }
}

package com.zutubi.pulse.committransformers;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Text;
import com.zutubi.validation.annotations.Required;

/**
 * <class comment/>
 */
@Form(fieldOrder = {"name", "url"})
public class JiraHandler implements CommitMessageHandler
{
    private String name;
    private String url;

    @Required @Text(size=50)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required @Text(size=50)
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}

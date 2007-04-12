package com.zutubi.pulse.committransformers;

import com.zutubi.validation.annotations.Required;
import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.Text;

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

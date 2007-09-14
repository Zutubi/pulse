package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.form.descriptor.annotation.Text;
import com.zutubi.validation.annotations.Pattern;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.PatternGroup;

/**
 * <class comment/>
 */
@Form(fieldOrder = {"name", "expression", "link"})
public class LinkHandler implements CommitMessageHandler
{
    private String name;
    private String expression;
    private String link;
    
    @Required @Text(size=50)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required @Pattern @Text(size=50)
    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    @Required @PatternGroup @Text(size=50)
    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

}

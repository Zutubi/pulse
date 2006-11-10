package com.zutubi.pulse.committransformers;

import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.validation.annotations.Required;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <class comment/>
 */
@Form(fieldOrder = {"name", "expression", "link"})
public class StandardHandler implements CommitMessageHandler
{
    private String name;
    private String expression;
    private String link;
    
    @Required
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required
    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    @Required
    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

}

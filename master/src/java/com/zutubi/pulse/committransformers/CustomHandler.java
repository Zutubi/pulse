package com.zutubi.pulse.committransformers;

import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Pattern;
import com.zutubi.prototype.annotation.Text;
import com.zutubi.prototype.annotation.Form;

/**
 * <class comment/>
 */
@Form(fieldOrder = {"name", "expression", "replacement"})
public class CustomHandler implements CommitMessageHandler
{
    private String name;
    private String expression;
    private String replacement;

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

    @Text(size=50)
    public String getReplacement()
    {
        return replacement;
    }

    public void setReplacement(String replacement)
    {
        this.replacement = replacement;
    }
}

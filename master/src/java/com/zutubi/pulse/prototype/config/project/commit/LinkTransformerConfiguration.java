package com.zutubi.pulse.prototype.config.project.commit;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.committransformers.Substitution;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.Arrays;
import java.util.List;

/**
 * A transformer that simplifies the common case of turning some text into a
 * link.
 */
@SymbolicName("zutubi.linkTransformerConfig")
@Form(fieldOrder = {"name", "expression", "url", "exclusive"})
public class LinkTransformerConfiguration extends CommitMessageTransformerConfiguration
{
    @Required
    @ValidRegex
    private String expression;
    @Required
    private String url;

    public LinkTransformerConfiguration()
    {
    }

    public LinkTransformerConfiguration(String name, String expression, String url)
    {
        setName(name);
        this.expression = expression;
        this.url = url;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public List<Substitution> substitutions()
    {
        return Arrays.asList(new Substitution(expression, "<a href='" + url + "'>$0</a>", isExclusive()));
    }
}

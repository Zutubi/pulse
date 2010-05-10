package com.zutubi.pulse.master.tove.config.project.commit;

import com.zutubi.pulse.master.committransformers.SimpleSubstitution;
import com.zutubi.pulse.master.committransformers.Substitution;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.Arrays;
import java.util.List;

/**
 * The most flexible commit message transformer where the user controls the
 * expression and replacement.
 */
@SymbolicName("zutubi.customTransformerConfig")
@Form(fieldOrder = {"name", "expression", "replacement", "exclusive"})
public class CustomTransformerConfiguration extends CommitMessageTransformerConfiguration
{
    @Required
    @ValidRegex
    private String expression;
    private String replacement;

    public CustomTransformerConfiguration()
    {
    }

    public CustomTransformerConfiguration(String name, String expression, String replacement)
    {
        setName(name);
        this.expression = expression;
        this.replacement = replacement;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public String getReplacement()
    {
        return replacement;
    }

    public void setReplacement(String replacement)
    {
        this.replacement = replacement;
    }

    public List<Substitution> substitutions()
    {
        return Arrays.<Substitution>asList(new SimpleSubstitution(expression, replacement));
    }
}

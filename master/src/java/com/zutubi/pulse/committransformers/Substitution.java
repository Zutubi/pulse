package com.zutubi.pulse.committransformers;

/**
 * Represents a single substitution to be made by a commit message builder. 
 */
public class Substitution
{
    private String expression;
    private String replacement;

    public Substitution(String expression, String replacement)
    {
        this.expression = expression;
        this.replacement = replacement;
    }

    public String getExpression()
    {
        return expression;
    }

    public String getReplacement()
    {
        return replacement;
    }
}

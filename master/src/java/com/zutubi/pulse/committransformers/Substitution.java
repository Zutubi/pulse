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

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Substitution that = (Substitution) o;
        return expression.equals(that.expression) && replacement.equals(that.replacement);
    }

    public int hashCode()
    {
        int result;
        result = expression.hashCode();
        result = 31 * result + replacement.hashCode();
        return result;
    }
}

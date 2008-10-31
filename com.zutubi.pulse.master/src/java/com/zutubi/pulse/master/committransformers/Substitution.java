package com.zutubi.pulse.master.committransformers;

/**
 * Represents a single substitution to be made by a commit message builder. 
 */
public class Substitution
{
    private String expression;
    private String replacement;
    private boolean exclusive;

    public Substitution(String expression, String replacement, boolean exclusive)
    {
        this.expression = expression;
        this.replacement = replacement;
        this.exclusive = exclusive;
    }

    public String getExpression()
    {
        return expression;
    }

    public String getReplacement()
    {
        return replacement;
    }

    public boolean isExclusive()
    {
        return exclusive;
    }

    public void setExclusive(boolean exclusive)
    {
        this.exclusive = exclusive;
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

        if (exclusive != that.exclusive)
        {
            return false;
        }
        if (!expression.equals(that.expression))
        {
            return false;
        }
        return replacement.equals(that.replacement);
    }

    public int hashCode()
    {
        int result;
        result = expression.hashCode();
        result = 31 * result + replacement.hashCode();
        result = 31 * result + (exclusive ? 1 : 0);
        return result;
    }
}

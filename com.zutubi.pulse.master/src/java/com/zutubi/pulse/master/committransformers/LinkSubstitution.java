package com.zutubi.pulse.master.committransformers;

/**
 * A substitution that wraps matched text in a link.
 */
public class LinkSubstitution implements Substitution
{
    private String expression;
    private String linkUrl;
    private String linkText;

    public LinkSubstitution(String expression, String linkUrl, String linkText)
    {
        this.expression = expression;
        this.linkUrl = linkUrl;
        this.linkText = linkText;
    }

    public String getExpression()
    {
        return expression;
    }

    public String getLinkUrl()
    {
        return linkUrl;
    }

    public String getLinkText()
    {
        return linkText;
    }

    public String getReplacement()
    {
        return "<a href='" + linkUrl + "'>" + linkText + "</a>";
    }

    @Override
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

        LinkSubstitution that = (LinkSubstitution) o;

        if (!expression.equals(that.expression))
        {
            return false;
        }
        if (!linkText.equals(that.linkText))
        {
            return false;
        }
        if (!linkUrl.equals(that.linkUrl))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = expression.hashCode();
        result = 31 * result + linkUrl.hashCode();
        result = 31 * result + linkText.hashCode();
        return result;
    }
}

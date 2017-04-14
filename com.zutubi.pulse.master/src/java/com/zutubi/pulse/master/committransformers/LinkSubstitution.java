/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

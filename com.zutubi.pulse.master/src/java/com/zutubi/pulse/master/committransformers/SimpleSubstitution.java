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
 * Represents a single substitution to be made by a commit message builder. 
 */
public class SimpleSubstitution implements Substitution
{
    private String expression;
    private String replacement;

    public SimpleSubstitution(String expression, String replacement)
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

        SimpleSubstitution that = (SimpleSubstitution) o;

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
        return result;
    }
}

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

package com.zutubi.validation.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks a field value matches a regular expression.
 */
public class RegexValidator extends StringFieldValidatorSupport
{
    private String pattern = ".";
    private boolean caseSensitive = true;

    public void validateStringField(String value)
    {
        Pattern pattern;
        if (isCaseSensitive())
        {
            pattern = Pattern.compile(getPattern());
        }
        else
        {
            pattern = Pattern.compile(getPattern(), Pattern.CASE_INSENSITIVE);
        }

        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        {
            addError();
        }
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return this.pattern;
    }

    /**
     * @return Returns whether the expression should be matched against in a case-sensitive way.
     *         Default is <code>true</code>.
     */
    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

    /**
     * Sets whether the expression should be matched against in a case-sensitive way.
     * Default is <code>true</code>.
     * 
     * @param caseSensitive true to make matching case sensitive
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }
}

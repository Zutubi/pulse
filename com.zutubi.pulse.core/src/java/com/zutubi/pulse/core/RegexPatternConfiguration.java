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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Configures a single pattern as part of a larger {@link com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration}.
 */
@SymbolicName("zutubi.regexPatternConfig")
@Form(fieldOrder = {"category", "expression", "exclusions", "summary"})
@Table(columns = {"category", "expression"})
public class RegexPatternConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    private Feature.Level category = Feature.Level.ERROR;
    @Required @ValidRegex
    private String expression;
    private String summary;
    @Addable(value = "exclude", attribute = "expression") @StringList
    private List<String> exclusions = new LinkedList<String>();

    public RegexPatternConfiguration()
    {
    }

    public RegexPatternConfiguration(Feature.Level category, Pattern pattern)
    {
        this.category = category;
        this.expression = pattern.pattern();
        exclusions = new LinkedList<String>();
    }

    public Feature.Level getCategory()
    {
        return category;
    }

    public void setCategory(Feature.Level level)
    {
        this.category = level;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public List<String> getExclusions()
    {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions)
    {
        this.exclusions = exclusions;
    }

    public void addExclusion(String expression)
    {
        exclusions.add(expression);
    }

    public void validate(ValidationContext context)
    {
        for (String expression: exclusions)
        {
            try
            {
                Pattern.compile(expression);
            }
            catch (PatternSyntaxException e)
            {
                context.addFieldError("exclusions", "Invalid expression '" + expression + "': " + e.getMessage());
            }
        }
    }
}

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

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

import com.zutubi.pulse.master.committransformers.LinkSubstitution;
import com.zutubi.pulse.master.committransformers.Substitution;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.Arrays;
import java.util.List;

/**
 * A transformer that simplifies the common case of turning some text into a
 * link.
 */
@SymbolicName("zutubi.linkTransformerConfig")
@Form(fieldOrder = {"name", "expression", "url", "exclusive"})
public class LinkTransformerConfiguration extends CommitMessageTransformerConfiguration
{
    @Required
    @ValidRegex
    private String expression;
    @Required
    private String url;

    public LinkTransformerConfiguration()
    {
    }

    public LinkTransformerConfiguration(String name, String expression, String url)
    {
        setName(name);
        this.expression = expression;
        this.url = url;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public List<Substitution> substitutions()
    {
        return Arrays.<Substitution>asList(new LinkSubstitution(expression, url, "$0"));
    }
}

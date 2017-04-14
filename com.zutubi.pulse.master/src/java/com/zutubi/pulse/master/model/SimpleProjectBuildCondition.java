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

package com.zutubi.pulse.master.model;

import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A condition that is a disjunction of some simple conditions.
 */
public class SimpleProjectBuildCondition extends ProjectBuildCondition
{
    private String expression;

    public SimpleProjectBuildCondition()
    {
    }

    public SimpleProjectBuildCondition(String expression)
    {
        this.expression = expression;
    }

    public SimpleProjectBuildCondition(List<String> conditions)
    {
        setConditions(conditions);
    }

    public String getType()
    {
        return "simple";
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public List<String> getConditions()
    {
        return Arrays.asList(expression.split(" or "));
    }

    public void setConditions(List<String> conditions)
    {
        expression = StringUtils.join(" or ", conditions.toArray(new String[conditions.size()]));
    }
}

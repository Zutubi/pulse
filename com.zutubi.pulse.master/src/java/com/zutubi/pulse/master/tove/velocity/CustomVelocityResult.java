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

package com.zutubi.pulse.master.tove.velocity;

import com.opensymphony.webwork.dispatcher.VelocityResult;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

/**
 * An extension of the default webwork Velocity Result that uses our internal
 * template loading.
 *
 * If the context does not contain a custom loaded template, then we defer to the
 * default loading.
 */
public class CustomVelocityResult extends VelocityResult
{
    /**
     * The property by which the template can be retrieved from the ognl stack.
     */
    private static final String PROPERTY_TEMPLATE = "template";

    protected Template getTemplate(OgnlValueStack stack, VelocityEngine velocity, ActionInvocation invocation, String location, String encoding) throws Exception
    {
        // since we are already within a velocity implementation, we are going to assume that the
        // preloaded template is also a velocity implementation.
        Template template = (Template) invocation.getStack().findValue(PROPERTY_TEMPLATE, Template.class);
        if (template != null)
        {
            return template;
        }
        return super.getTemplate(stack, velocity, invocation, location, encoding);
    }
}

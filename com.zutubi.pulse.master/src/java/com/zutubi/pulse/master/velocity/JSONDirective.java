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

package com.zutubi.pulse.master.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.Writer;
import java.io.IOException;

import flexjson.JSONSerializer;
import freemarker.template.utility.StringUtil;

/**
 * The JSON Directive is a velocity directive that takes a
 * argument and serialises it to a JSON format.
 */
public class JSONDirective extends AbstractDirective
{
    public String getName()
    {
        return "json";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Object o = node.jjtGetChild(0).value(context);
        if (o != null)
        {
            JSONSerializer serializer = new JSONSerializer();
            serializer.exclude("*.class");

            String json = serializer.serialize(o);
            writer.write(StringUtil.javaScriptStringEnc(json));
        }
        return true;
    }
}

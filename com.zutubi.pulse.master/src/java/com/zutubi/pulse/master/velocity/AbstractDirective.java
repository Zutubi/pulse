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

import com.opensymphony.xwork.util.OgnlValueStack;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public abstract class AbstractDirective extends Directive
{
    /**
     * Extract the directives body content. That is, anything between the directive
     * declaration and the end directive.
     *
     * #directive()
     *     bodyContent
     * #end
     *
     * @param node
     * @param context
     * @return
     * @throws java.io.IOException
     * @throws org.apache.velocity.exception.MethodInvocationException
     * @throws org.apache.velocity.exception.ParseErrorException
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     */
    protected String extractBodyContext(Node node, InternalContextAdapter context)
            throws IOException, MethodInvocationException, ParseErrorException, ResourceNotFoundException
    {
        StringWriter bodyContent = new StringWriter(1024);
        
        int bodyNode = node.jjtGetNumChildren() - 1;
        node.jjtGetChild(bodyNode).render(context, bodyContent);

        return bodyContent.toString();
    }

    /**
     * Pushes this component on to the stack and then copies the supplied parameters over.
     *
     * @param params
     */
    protected void wireParams(Map params)
    {
        OgnlValueStack stack = new OgnlValueStack();
        stack.push(this);
        for (Object o : params.entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            stack.setValue(key, entry.getValue());
        }
    }

    /**
     * Create a Map of properties that the user has passed in. For example,
     * <pre>
     * #xxx("name=hello" "value=world" "template=foo")
     * </pre>
     * would yield a params that contains {["name", "hello"], ["value", "world"], ["template", "foo"]}
     *
     * @param node the Node passed in to the render method
     *
     * @return a Map of the user specified properties
     *
     * @throws org.apache.velocity.exception.ParseErrorException
     *          if the was an error in the format of the property
     */
    protected Map createPropertyMap(InternalContextAdapter contextAdapter, Node node) throws ParseErrorException
    {
        Map<String, String> propertyMap = new HashMap<String, String>();

        int children = node.jjtGetNumChildren();
        if (getType() == BLOCK)
        {
            children--;
        }

        for (int index = 0, length = children; index < length; index++)
        {
            try
            {
                this.putProperty(propertyMap, contextAdapter, node.jjtGetChild(index));
            }
            catch (MethodInvocationException e)
            {
                throw new ParseErrorException("Failed to retrieve the requested property.");
            }
        }

        return propertyMap;
    }

    /**
     * Adds a given Node's key/value pair to the propertyMap.  For example, if this Node contained the value "rows=20",
     * then the key, rows, would be added to the propertyMap with the String value, 20.
     *
     * @param propertyMap a params containing all the properties that we wish to set
     * @param node        the parameter to set expressed in "name=value" format
     */
    protected void putProperty(Map<String, String> propertyMap, InternalContextAdapter contextAdapter, Node node) throws ParseErrorException, MethodInvocationException
    {
        String param = node.value(contextAdapter).toString();
        int idx = param.indexOf("=");
        if (idx != -1)
        {
            String property = param.substring(0, idx);
            String value = param.substring(idx + 1);
            propertyMap.put(property, value);
        }
        else
        {
            throw new ParseErrorException("#" + this.getName() + " arguments must include an assignment operator!  For example #tag( Component \"template=mytemplate\" ).  #tag( TextField \"mytemplate\" ) is illegal!");
        }
    }
}

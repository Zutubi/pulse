package com.zutubi.pulse.master.velocity;

import com.zutubi.pulse.core.util.api.XMLUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * A directive that encodes its content to escape XML special characters.
 */
public class XmlEncodeDirective extends Directive
{
    public String getName()
    {
        return "xml";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        String in = String.valueOf(node.jjtGetChild(0).value(context));
        writer.write(XMLUtils.escape(in));
        return true;
    }
}
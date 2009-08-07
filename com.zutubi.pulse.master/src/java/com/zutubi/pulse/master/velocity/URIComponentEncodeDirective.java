package com.zutubi.pulse.master.velocity;

import com.zutubi.util.WebUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * A directive that encodes its content as a component of a URI.  All
 * characters that cannot appear literally in a URI component are escaped.
 * This includes the / character, as the content should only be a single
 * path component (if part of a path at all).
 */
public class URIComponentEncodeDirective extends Directive
{
    public String getName()
    {
        return "uce";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        String in = String.valueOf(node.jjtGetChild(0).value(context));
        writer.write(WebUtils.uriComponentEncode(in));
        return true;
    }
}

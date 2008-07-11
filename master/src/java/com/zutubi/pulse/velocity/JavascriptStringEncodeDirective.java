package com.zutubi.pulse.velocity;

import freemarker.template.utility.StringUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * A directive that encodes its content to escape Javascript string special
 * characters (like ?js_string in FreeMarker).
 */
public class JavascriptStringEncodeDirective extends Directive
{
    public String getName()
    {
        return "jss";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        String in = String.valueOf(node.jjtGetChild(0).value(context));
        writer.write(StringUtil.javaStringEnc(in));
        return true;
    }
}

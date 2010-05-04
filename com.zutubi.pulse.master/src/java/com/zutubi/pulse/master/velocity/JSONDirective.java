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

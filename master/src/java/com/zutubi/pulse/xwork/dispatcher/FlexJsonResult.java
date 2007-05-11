package com.zutubi.pulse.xwork.dispatcher;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.WebWorkResultSupport;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import flexjson.JSONSerializer;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A Webwork result that outputs a JSON string, using Flexjson for trivial
 * serialisation from a Java object.
 */
public class FlexJsonResult extends WebWorkResultSupport
{
    protected void doExecute(String finalLocation, ActionInvocation ai) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        OgnlValueStack stack = ai.getStack();

        JSONSerializer serializer = new JSONSerializer();
        serializer.setStopClass(Object.class);
        String json = serializer.serialize(stack.findValue(finalLocation));
        Writer writer = new OutputStreamWriter(response.getOutputStream(), response.getCharacterEncoding());
        writer.write(json);
        response.setContentType("application/json"); // opera does not like this...
        writer.flush();
    }

}

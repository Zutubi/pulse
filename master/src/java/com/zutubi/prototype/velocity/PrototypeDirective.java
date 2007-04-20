package com.zutubi.prototype.velocity;

import com.zutubi.pulse.velocity.AbstractDirective;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.xwork.ActionContext;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import freemarker.core.DelegateBuiltin;

/**
 *
 *
 */
public abstract class PrototypeDirective extends AbstractDirective
{
    public PrototypeDirective()
    {
        ComponentContext.autowire(this);
    }

    protected Type lookupType()
    {
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        return (Type) stack.findValue("type");
    }

    protected Messages lookupMessages()
    {
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        return (Messages) stack.findValue("messages");
    }

    protected Record lookupRecord()
    {
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        return (Record) stack.findValue("record");
    }

    protected String lookupPath()
    {
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        return (String) stack.findValue("path");
    }

    protected String renderError(String errorMessage) throws IOException
    {
        return "<span id=\"error\">" + errorMessage + "</span>";
    }

    protected Map<String, Object> initialiseContext(Class clazz)
    {
        Map<String, Object> context = new HashMap<String, Object>();
        Messages messages = Messages.getInstance(clazz);
        context.put("i18nText", new GetTextMethod(messages));

        // validation support:
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        context.put("fieldErrors", stack.findValue("fieldErrors"));

        // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
        DelegateBuiltin.conditionalRegistration("i18n", "i18nText");
        return context;
    }
}

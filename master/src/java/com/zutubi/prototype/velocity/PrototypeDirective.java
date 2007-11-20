package com.zutubi.prototype.velocity;

import com.zutubi.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.freemarker.BaseNameMethod;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.freemarker.ValidIdMethod;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.velocity.AbstractDirective;
import freemarker.core.DelegateBuiltin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public abstract class PrototypeDirective extends AbstractDirective
{
    private String property;

    public PrototypeDirective()
    {
        ComponentContext.autowire(this);
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    protected Type lookupType()
    {
        return (Type) lookup("type");
    }

    protected Messages lookupMessages()
    {
        return (Messages) lookup("messages");
    }

    protected Record lookupRecord()
    {
        return (Record) lookup("record");
    }

    protected String lookupPath()
    {
        return (String) lookup("path");
    }

    protected Object lookup(String key)
    {
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        if(TextUtils.stringSet(property))
        {
            key = property + "." + key;
        }
        
        return stack.findValue(key);
    }

    protected String renderError(String errorMessage) throws IOException
    {
        return "<span id=\"error\">" + errorMessage + "</span>";
    }

    public static Map<String, Object> initialiseContext(Class clazz)
    {
        Map<String, Object> context = new HashMap<String, Object>();
        Messages messages = Messages.getInstance(clazz);
        context.put("i18nText", new GetTextMethod(messages));
        context.put("baseName", new BaseNameMethod());
        context.put("validId", new ValidIdMethod());

        // validation support:
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        context.put("fieldErrors", stack.findValue("fieldErrors"));

        // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
        DelegateBuiltin.conditionalRegistration("i18n", "i18nText");
        DelegateBuiltin.conditionalRegistration("baseName", "baseName");
        DelegateBuiltin.conditionalRegistration("id", "validId");
        return context;
    }
}

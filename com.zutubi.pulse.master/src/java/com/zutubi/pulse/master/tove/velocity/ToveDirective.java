package com.zutubi.pulse.master.tove.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.velocity.AbstractDirective;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.StringUtils;

import java.io.IOException;

/**
 *
 *
 */
public abstract class ToveDirective extends AbstractDirective
{
    private String property;

    public ToveDirective()
    {
        SpringComponentContext.autowire(this);
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
        if(StringUtils.stringSet(property))
        {
            key = property + "." + key;
        }
        
        return stack.findValue(key);
    }

    protected String renderError(String errorMessage) throws IOException
    {
        return "<span id=\"error\">" + errorMessage + "</span>";
    }
}

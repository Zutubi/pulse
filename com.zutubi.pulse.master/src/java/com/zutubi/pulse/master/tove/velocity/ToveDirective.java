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

package com.zutubi.pulse.form.ui.components;

import com.opensymphony.webwork.util.MakeIterator;
import com.opensymphony.webwork.util.ContainUtil;

import java.util.Collection;
import java.util.Map;
import java.lang.reflect.Array;

/**
 * <class-comment/>
 */
public abstract class ListUIComponent extends UIComponent
{
    protected Object list;
    protected String listKey;
    protected String listValue;

    public boolean contains(Object obj1, Object obj2)
    {
        return ContainUtil.contains(obj1, obj2);
    }

    public void evaluateExtraParameters()
    {
        Object value = null;

        if (list == null)
        {
            list = parameters.get("list");
        }

        if (list instanceof Collection || list instanceof Map)
        {
            value = list;
        }
        else if (MakeIterator.isIterable(list))
        {
            value = MakeIterator.convert(list);
        }
        if (value == null)
        {
            throw new RuntimeException();
        }

        if (value instanceof Collection)
        {
            addParameter("list", value);
        }
        else
        {
            addParameter("list", MakeIterator.convert(value));
        }

        if (value instanceof Collection)
        {
            addParameter("listSize", new Integer(((Collection) value).size()));
        }
        else if (value instanceof Map)
        {
            addParameter("listSize", new Integer(((Map) value).size()));
        }
        else if (value != null && value.getClass().isArray())
        {
            addParameter("listSize", new Integer(Array.getLength(value)));
        }

        if (listKey != null)
        {
            addParameter("listKey", listKey);
        }
        else if (value instanceof Map)
        {
            addParameter("listKey", "key");
        }

        if (listValue != null)
        {
            addParameter("listValue", listValue);
        }
        else if (value instanceof Map)
        {
            addParameter("listValue", "value");
        }
    }

    public void setList(Object list)
    {
        this.list = list;
    }

    public void setListKey(String listKey)
    {
        this.listKey = listKey;
    }

    public void setListValue(String listValue)
    {
        this.listValue = listValue;
    }

}

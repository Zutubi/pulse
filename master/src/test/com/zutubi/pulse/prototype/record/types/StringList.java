package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.Arrays;
import java.util.List;

/**
 */
@SymbolicName("stringlist")
public class StringList
{
    private List<String> list;

    public StringList()
    {
    }

    public StringList(String... values)
    {
        list = Arrays.asList(values);
    }

    public List<String> getList()
    {
        return list;
    }

    public void setList(List<String> list)
    {
        this.list = list;
    }


    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof StringList))
        {
            return false;
        }

        return list.equals(((StringList)obj).list);
    }
}

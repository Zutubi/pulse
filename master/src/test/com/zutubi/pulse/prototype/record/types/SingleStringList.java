package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;

import java.util.Arrays;
import java.util.List;

/**
 * @deprecated
 */
@SymbolicName("singlestringlist")
public class SingleStringList
{
    private List<SingleString> list;

    public SingleStringList()
    {
    }

    public SingleStringList(String... values)
    {
        list = CollectionUtils.map(Arrays.asList(values), new Mapping<String, SingleString>()
        {
            public SingleString map(String s)
            {
                return new SingleString(s);
            }
        });
    }

    public List<SingleString> getList()
    {
        return list;
    }

    public void setList(List<SingleString> list)
    {
        this.list = list;
    }


    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof SingleStringList))
        {
            return false;
        }

        return list.equals(((SingleStringList)obj).list);
    }
}

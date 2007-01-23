package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.Arrays;
import java.util.List;

/**
 */
@SymbolicName("polylist")
public class PolymorphicList
{
    private List<ParentType> list;

    public PolymorphicList()
    {
    }

    public PolymorphicList(ParentType ...items)
    {
        list = Arrays.asList(items);
    }

    public List<ParentType> getList()
    {
        return list;
    }

    public void setList(List<ParentType> list)
    {
        this.list = list;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PolymorphicList that = (PolymorphicList) o;
        return !(list != null ? !list.equals(that.list) : that.list != null);
    }

    public int hashCode()
    {
        return (list != null ? list.hashCode() : 0);
    }
}

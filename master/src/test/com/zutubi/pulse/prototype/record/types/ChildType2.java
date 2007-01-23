package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.Arrays;
import java.util.List;

/**
 */
@SymbolicName("child2")
public class ChildType2 extends ParentType
{
    private List<String> list;

    public ChildType2()
    {
    }

    public ChildType2(int someInt, String... strings)
    {
        super(someInt);
        this.list = Arrays.asList(strings);
    }

    public List<String> getList()
    {
        return list;
    }

    public void setList(List<String> list)
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
        if (!super.equals(o))
        {
            return false;
        }

        ChildType2 that = (ChildType2) o;
        return !(list != null ? !list.equals(that.list) : that.list != null);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }
}
